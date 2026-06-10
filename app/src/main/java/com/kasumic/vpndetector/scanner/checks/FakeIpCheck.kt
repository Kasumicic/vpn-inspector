package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class FakeIpCheck(private val context: Context) {
    suspend fun run(): ScanResult = withContext(Dispatchers.IO) {
        val targetHosts = listOf("ya.ru", "google.com", "mail.ru")
        val fakeRanges = listOf("198.18.", "198.19.", "10.", "172.16.", "192.168.")
        val foundFakeIps = mutableListOf<String>()

        for (host in targetHosts) {
            try {
                val address = InetAddress.getByName(host).hostAddress ?: continue
                for (range in fakeRanges) {
                    if (address.startsWith(range)) {
                        foundFakeIps.add("$host->$address")
                        break
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (foundFakeIps.isNotEmpty()) {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.fake_ip_title),
                true,
                context.getString(R.string.fake_ip_detected, foundFakeIps.joinToString(", ")),
                context.getString(R.string.fake_ip_desc),
                context.getString(R.string.fake_ip_fix)
            )
        } else {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.fake_ip_title),
                false,
                context.getString(R.string.fake_ip_clean),
                context.getString(R.string.fake_ip_desc_clean),
                ""
            )
        }
    }
}
