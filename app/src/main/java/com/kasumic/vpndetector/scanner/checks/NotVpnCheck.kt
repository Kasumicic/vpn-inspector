package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult

class NotVpnCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.not_vpn_title),
                false,
                context.getString(R.string.not_vpn_no_network),
                context.getString(R.string.not_vpn_desc),
                ""
            )
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.not_vpn_title),
                false,
                context.getString(R.string.not_vpn_no_data),
                context.getString(R.string.not_vpn_desc),
                ""
            )

            val isNotVpnCap = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

            if (!isNotVpnCap) {
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.not_vpn_title),
                    isRisky = true,
                    details = context.getString(R.string.not_vpn_missing),
                    description = context.getString(R.string.not_vpn_desc),
                    fixSuggestion = context.getString(R.string.not_vpn_fix)
                )
            } else {
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.not_vpn_title),
                    isRisky = false,
                    details = context.getString(R.string.not_vpn_clean),
                    description = context.getString(R.string.not_vpn_desc),
                    fixSuggestion = ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.not_vpn_title),
                false,
                context.getString(R.string.direct_vpn_error, e.message ?: ""),
                "",
                ""
            )
        }
    }
}
