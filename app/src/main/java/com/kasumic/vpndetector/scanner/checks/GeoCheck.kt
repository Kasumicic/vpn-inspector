package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.api.NetworkClient
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GeoResult(
    val ip: String,
    val countryCode: String?,
    val results: List<ScanResult>
)

class GeoCheck(private val context: Context) {
    suspend fun run(): GeoResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<ScanResult>()
        var ip = context.getString(R.string.unknown_val)
        var countryCode: String? = null
        try {
            val response = NetworkClient.ipApiService.getIpInfo()
            if (response.ip != null) {
                ip = response.ip
                countryCode = response.country?.iso ?: context.getString(R.string.unknown_val)
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val targetRegion = sharedPrefs.getString("target_region", "RU") ?: "RU"
                val notTarget = !countryCode.equals(targetRegion, ignoreCase = true)

                val isRisky = notTarget
                results.add(
                    ScanResult(
                        category = ScanCategory.GEO,
                        moduleName = context.getString(R.string.geo_analysis_title),
                        isRisky = isRisky,
                        details = if (isRisky) {
                            context.getString(R.string.geo_detail_suspicious, countryCode)
                        } else {
                            context.getString(R.string.geo_detail_clean, countryCode)
                        },
                        description = context.getString(R.string.geo_desc),
                        fixSuggestion = if (isRisky) {
                            context.getString(R.string.geo_fix_suspicious)
                        } else {
                            context.getString(R.string.geo_fix_clean)
                        }
                    )
                )
            } else {
                results.add(ScanResult(
                    category = ScanCategory.GEO,
                    moduleName = context.getString(R.string.geo_analysis_title),
                    isRisky = false,
                    details = context.getString(R.string.geo_error_network),
                    description = context.getString(R.string.geo_error_server),
                    fixSuggestion = "",
                    isError = true
                ))
            }
        } catch (e: Exception) {
            try {
                val request = okhttp3.Request.Builder().url("https://api64.ipify.org").build()
                val response = NetworkClient.okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    ip = response.body?.string()?.trim() ?: ip
                }
            } catch (fallbackE: Exception) {
                // Ignore fallback exception
            }
            results.add(ScanResult(
                category = ScanCategory.GEO,
                moduleName = context.getString(R.string.geo_analysis_title),
                isRisky = false,
                details = context.getString(R.string.geo_error_prefix, e.message ?: ""),
                description = context.getString(R.string.geo_error_connect),
                fixSuggestion = "",
                isError = true
            ))
        }
        return@withContext GeoResult(ip, countryCode, results)
    }
}
