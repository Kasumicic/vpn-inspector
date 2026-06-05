package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import java.net.NetworkInterface

class InterfacesCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            val badIfaces = mutableListOf<String>()
            for (iface in interfaces) {
                val name = iface.name.lowercase()
                if (name.contains("tun") || name.contains("ppp") || name.contains("tap") || name.contains("wg") || name.contains("ipsec")) {
                    badIfaces.add(name)
                }
            }
            if (badIfaces.isNotEmpty()) {
                ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.interfaces_title),
                    true,
                    context.getString(R.string.interfaces_found, badIfaces.joinToString(", ")),
                    context.getString(R.string.interfaces_desc),
                    context.getString(R.string.interfaces_fix)
                )
            } else {
                ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.interfaces_title),
                    false,
                    context.getString(R.string.interfaces_clean),
                    context.getString(R.string.interfaces_desc_clean),
                    ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.interfaces_title),
                false,
                context.getString(R.string.direct_vpn_error, e.message ?: ""),
                context.getString(R.string.interfaces_error),
                ""
            )
        }
    }
}
