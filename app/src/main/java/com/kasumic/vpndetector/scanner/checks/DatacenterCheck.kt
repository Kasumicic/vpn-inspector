package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.api.NetworkClient
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatacenterCheck(private val context: Context) {
    suspend fun run(ip: String?): ScanResult = withContext(Dispatchers.IO) {
        try {
            val response = if (ip != null && ip != context.getString(R.string.unknown_val) && ip.isNotEmpty()) {
                NetworkClient.ipApiIsService.getIpTypeInfoFor(ip)
            } else {
                NetworkClient.ipApiIsService.getIpTypeInfo()
            }

            val isDatacenter = response.is_datacenter == true
            val isVpn = response.is_vpn == true
            val isProxy = response.is_proxy == true
            val isTor = response.is_tor == true
            val isCrawler = response.is_crawler == true

            val isRisky = isDatacenter || isVpn || isProxy || isTor

            val details = if (isRisky) {
                val providerName = response.datacenter?.datacenter ?: context.getString(R.string.unknown_val)
                context.getString(
                    R.string.datacenter_check_detected,
                    providerName,
                    if (isVpn) "YES" else "NO",
                    if (isProxy) "YES" else "NO",
                    if (isTor) "YES" else "NO"
                )
            } else {
                val providerName = if (response.datacenter != null) {
                    response.datacenter.datacenter ?: "Consumer ISP"
                } else {
                    "Consumer ISP"
                }
                val typeDetails = if (isCrawler) "Crawler" else "Residential"
                context.getString(R.string.datacenter_check_clean, providerName, typeDetails)
            }

            ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.datacenter_check_title),
                isRisky = isRisky,
                details = details,
                description = if (isRisky) {
                    context.getString(R.string.datacenter_check_desc)
                } else {
                    context.getString(R.string.datacenter_check_desc_clean)
                },
                fixSuggestion = if (isRisky) {
                    context.getString(R.string.datacenter_check_fix)
                } else {
                    context.getString(R.string.geo_fix_clean)
                }
            )
        } catch (e: Exception) {
            ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.datacenter_check_title),
                isRisky = false,
                details = context.getString(R.string.datacenter_check_error, e.message ?: ""),
                description = context.getString(R.string.datacenter_check_desc_clean),
                fixSuggestion = "",
                isError = true
            )
        }
    }
}
