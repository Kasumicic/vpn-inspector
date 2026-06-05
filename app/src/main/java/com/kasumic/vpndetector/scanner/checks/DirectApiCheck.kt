package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult

class DirectApiCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(
                ScanCategory.DIRECT,
                context.getString(R.string.direct_vpn_api_title),
                false,
                context.getString(R.string.direct_vpn_no_network),
                context.getString(R.string.direct_vpn_desc),
                ""
            )
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ScanResult(
                ScanCategory.DIRECT,
                context.getString(R.string.direct_vpn_api_title),
                false,
                context.getString(R.string.direct_vpn_no_data),
                context.getString(R.string.direct_vpn_desc),
                ""
            )

            val hasVpnTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            var hasVpnInfo = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val tInfo = caps.transportInfo
                if (tInfo != null && tInfo.javaClass.name == "android.net.VpnTransportInfo") {
                    hasVpnInfo = true
                }
            }

            if (hasVpnTransport || hasVpnInfo) {
                val detailDesc = if (hasVpnInfo) "(TRANSPORT_VPN & VpnTransportInfo)" else "(NetworkCapabilities)"
                ScanResult(
                    ScanCategory.DIRECT,
                    context.getString(R.string.direct_vpn_api_title),
                    true,
                    context.getString(R.string.direct_vpn_found, detailDesc),
                    context.getString(R.string.direct_vpn_found_desc),
                    context.getString(R.string.direct_vpn_fix)
                )
            } else {
                ScanResult(
                    ScanCategory.DIRECT,
                    context.getString(R.string.direct_vpn_api_title),
                    false,
                    context.getString(R.string.direct_vpn_clean),
                    context.getString(R.string.direct_vpn_desc),
                    ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                ScanCategory.DIRECT,
                context.getString(R.string.direct_vpn_api_title),
                false,
                context.getString(R.string.direct_vpn_error, e.message ?: ""),
                "",
                ""
            )
        }
    }
}
