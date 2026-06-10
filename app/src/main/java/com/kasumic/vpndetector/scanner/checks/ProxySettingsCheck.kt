package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult

class ProxySettingsCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val host = System.getProperty("http.proxyHost") 
                ?: System.getProperty("https.proxyHost") 
                ?: System.getProperty("socksProxyHost")
            val port = System.getProperty("http.proxyPort") 
                ?: System.getProperty("https.proxyPort") 
                ?: System.getProperty("socksProxyPort")
            
            if (!host.isNullOrEmpty()) {
                ScanResult(
                    category = ScanCategory.DIRECT,
                    moduleName = context.getString(R.string.proxy_settings_title),
                    isRisky = true,
                    details = context.getString(R.string.proxy_settings_found, host, port),
                    description = context.getString(R.string.proxy_settings_desc),
                    fixSuggestion = context.getString(R.string.proxy_settings_fix)
                )
            } else {
                ScanResult(
                    category = ScanCategory.DIRECT,
                    moduleName = context.getString(R.string.proxy_settings_title),
                    isRisky = false,
                    details = context.getString(R.string.proxy_settings_clean),
                    description = context.getString(R.string.proxy_settings_desc),
                    fixSuggestion = ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                ScanCategory.DIRECT,
                context.getString(R.string.proxy_settings_title),
                false,
                context.getString(R.string.direct_vpn_error, e.message ?: ""),
                "",
                ""
            )
        }
    }
}
