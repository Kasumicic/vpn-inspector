package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import java.net.NetworkInterface

class MtuCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            val anomalies = mutableListOf<String>()

            for (iface in interfaces) {
                if (iface.isUp && !iface.isLoopback) {
                    val mtu = iface.mtu
                    val name = iface.name.lowercase()

                    if (name.contains("wlan") || name.contains("eth")) {
                        if (mtu in 1..1449) anomalies.add("$name (size $mtu)")
                    } else if (name.contains("rmnet")) {
                        if (mtu in 1..1349) anomalies.add("$name (size $mtu)")
                    } else if (mtu in 1..1399) {
                        anomalies.add("$name (size $mtu)")
                    }
                }
            }
            if (anomalies.isNotEmpty()) {
                ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.mtu_anomalies_title),
                    true,
                    context.getString(R.string.mtu_low, anomalies.joinToString(", ")),
                    context.getString(R.string.mtu_desc),
                    context.getString(R.string.mtu_fix)
                )
            } else {
                ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.mtu_anomalies_title),
                    false,
                    context.getString(R.string.mtu_clean),
                    context.getString(R.string.mtu_desc_clean),
                    ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.mtu_anomalies_title),
                false,
                context.getString(R.string.mtu_error, e.message ?: ""),
                "",
                ""
            )
        }
    }
}
