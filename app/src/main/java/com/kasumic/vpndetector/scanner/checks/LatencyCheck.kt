package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

class LatencyCheck(private val context: Context) {
    suspend fun run(): ScanResult = withContext(Dispatchers.IO) {
        fun getTcpPing(host: String, port: Int = 443): Int {
            return try {
                val address = InetAddress.getByName(host)
                val start = System.nanoTime()
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress(address, port), 1500)
                socket.close()
                val end = System.nanoTime()
                ((end - start) / 1_000_000).toInt() // В миллисекундах
            } catch (e: Exception) {
                999
            }
        }

        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val targetRegion = sharedPrefs.getString("target_region", "RU") ?: "RU"
        val isRuRegion = targetRegion.equals("RU", ignoreCase = true)

        if (isRuRegion) {
            val pingRu = getTcpPing("ya.ru")
            val pingEu = getTcpPing("google.com")

            if (pingRu >= 999 && pingEu >= 999) {
                return@withContext ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.snitch_title),
                    false,
                    context.getString(R.string.snitch_timeout),
                    context.getString(R.string.snitch_desc_rtt),
                    context.getString(R.string.snitch_timeout_fix)
                )
            }

            if (pingRu <= 2 || pingEu <= 2) {
                return@withContext ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.snitch_title),
                    true,
                    context.getString(R.string.snitch_intercept, pingRu.toString(), pingEu.toString()),
                    context.getString(R.string.snitch_intercept_desc),
                    context.getString(R.string.snitch_intercept_fix)
                )
            }

            val ratio = pingRu.toFloat() / if (pingEu > 0) pingEu else 1

            if (ratio > 3f && pingRu > 100) {
                return@withContext ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.snitch_title),
                    true,
                    context.getString(R.string.snitch_anomaly, pingRu.toString(), pingEu.toString()),
                    context.getString(R.string.snitch_anomaly_desc),
                    context.getString(R.string.snitch_anomaly_fix)
                )
            }

            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.snitch_title),
                false,
                context.getString(R.string.snitch_clean, pingRu.toString(), pingEu.toString()),
                context.getString(R.string.snitch_desc_rtt_clean),
                ""
            )
        } else {
            // Non-RU Region: skip Russian sites check entirely, check only proxy interception (e.g. google.com <= 2 ms)
            val pingEu = getTcpPing("google.com")

            if (pingEu >= 999) {
                return@withContext ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.snitch_title),
                    false,
                    context.getString(R.string.snitch_timeout),
                    context.getString(R.string.snitch_desc_rtt),
                    context.getString(R.string.snitch_timeout_fix)
                )
            }

            if (pingEu <= 2) {
                return@withContext ScanResult(
                    ScanCategory.INDIRECT,
                    context.getString(R.string.snitch_title),
                    true,
                    context.getString(R.string.snitch_intercept_single, pingEu.toString()),
                    context.getString(R.string.snitch_intercept_desc),
                    context.getString(R.string.snitch_intercept_fix)
                )
            }

            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.snitch_title),
                false,
                context.getString(R.string.snitch_clean_single, pingEu.toString()),
                context.getString(R.string.snitch_desc_rtt_clean),
                ""
            )
        }
    }
}
