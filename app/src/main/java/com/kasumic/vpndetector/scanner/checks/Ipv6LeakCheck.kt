package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.api.NetworkClient
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Ipv6LeakCheck(private val context: Context) {
    suspend fun run(lastIpv4CountryCode: String?): ScanResult = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder()
                .url("https://api6.ipify.org")
                .build()
            val response = NetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val ipv6 = response.body?.string()?.trim()
                if (!ipv6.isNullOrEmpty()) {
                    var ipv6Country: String? = null
                    try {
                        val geoResponse = NetworkClient.ipApiService.getIpInfoFor(ipv6)
                        ipv6Country = geoResponse.country?.iso
                    } catch (e: Exception) {
                        // ignore and try fallback
                    }

                    if (ipv6Country.isNullOrEmpty() || ipv6Country == "null") {
                        ipv6Country = fetchBackupGeoCountry(ipv6)
                    }

                    val finalIpv6Country = ipv6Country ?: context.getString(R.string.unknown_val)
                    val rsharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val rtargetRegion = rsharedPrefs.getString("target_region", "RU") ?: "RU"
                    val ipv4Country = lastIpv4CountryCode ?: rtargetRegion

                    val isUnknownIpv4 = ipv4Country == "Неизвестно" || ipv4Country == "Unknown" || ipv4Country == context.getString(R.string.unknown_val)
                    val isUnknownIpv6 = finalIpv6Country == "Неизвестно" || finalIpv6Country == "Unknown" || finalIpv6Country == context.getString(R.string.unknown_val)

                    val isLeak = if (!isUnknownIpv4 && !isUnknownIpv6) {
                        ipv4Country != finalIpv6Country
                    } else {
                        false
                    }

                    if (isLeak) {
                        ScanResult(
                            category = ScanCategory.INDIRECT,
                            moduleName = context.getString(R.string.ipv6_leak_title),
                            isRisky = true,
                            details = context.getString(R.string.ipv6_leak_detected, ipv4Country, ipv6, finalIpv6Country),
                            description = context.getString(R.string.ipv6_leak_desc),
                            fixSuggestion = context.getString(R.string.ipv6_leak_fix)
                        )
                    } else {
                        val detailsText = if (!isUnknownIpv6 && !isUnknownIpv4) {
                            context.getString(R.string.ipv6_leak_safe, ipv6, finalIpv6Country, ipv4Country)
                        } else if (!isUnknownIpv6) {
                            context.getString(R.string.ipv6_leak_clean_active, ipv6, finalIpv6Country)
                        } else {
                            context.getString(R.string.ipv6_leak_clean_unknown, ipv6)
                        }
                        ScanResult(
                            category = ScanCategory.INDIRECT,
                            moduleName = context.getString(R.string.ipv6_leak_title),
                            isRisky = false,
                            details = detailsText,
                            description = context.getString(R.string.ipv6_leak_desc_clean),
                            fixSuggestion = ""
                        )
                    }
                } else {
                    ScanResult(
                        category = ScanCategory.INDIRECT,
                        moduleName = context.getString(R.string.ipv6_leak_title),
                        isRisky = false,
                        details = context.getString(R.string.ipv6_leak_clean_no_addr),
                        description = context.getString(R.string.ipv6_leak_desc_clean),
                        fixSuggestion = ""
                    )
                }
            } else {
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.ipv6_leak_title),
                    isRisky = false,
                    details = context.getString(R.string.ipv6_leak_clean_host_unreachable),
                    description = context.getString(R.string.ipv6_leak_desc_clean),
                    fixSuggestion = ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.ipv6_leak_title),
                isRisky = false,
                details = context.getString(R.string.ipv6_leak_clean_not_supported),
                description = context.getString(R.string.ipv6_leak_desc_clean),
                fixSuggestion = ""
            )
        }
    }

    private fun fetchBackupGeoCountry(ip: String): String? {
        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipapi.co/$ip/json/")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"country_code\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipwho.is/$ip")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"country_code\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val request = okhttp3.Request.Builder()
                .url("https://freeipapi.com/api/json/$ip")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"countryCode\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return null
    }
}
