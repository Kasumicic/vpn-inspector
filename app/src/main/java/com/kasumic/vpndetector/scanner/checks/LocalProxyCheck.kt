package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

class LocalProxyCheck(private val context: Context) {
    suspend fun run(): ScanResult = withContext(Dispatchers.IO) {
        val suspiciousPorts = listOf(1080, 10808, 9050, 8080, 44010, 44012, 44014, 1081)
        val foundPorts = mutableListOf<Int>()

        for (port in suspiciousPorts) {
            try {
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress("127.0.0.1", port), 50)
                socket.close()
                foundPorts.add(port)
            } catch (e: Exception) {
                // Port closed or error
            }
        }
        if (foundPorts.isNotEmpty()) {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.local_proxy_title),
                true,
                context.getString(R.string.local_proxy_open, foundPorts.joinToString(", ")),
                context.getString(R.string.local_proxy_desc),
                context.getString(R.string.local_proxy_fix)
            )
        } else {
            ScanResult(
                ScanCategory.INDIRECT,
                context.getString(R.string.local_proxy_title),
                false,
                context.getString(R.string.local_proxy_clean),
                context.getString(R.string.local_proxy_desc_clean),
                ""
            )
        }
    }
}
