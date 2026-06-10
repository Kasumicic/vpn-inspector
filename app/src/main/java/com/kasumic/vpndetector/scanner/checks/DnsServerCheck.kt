package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import android.net.ConnectivityManager
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult

class DnsServerCheck(private val context: Context) {
    fun run(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.dns_check_title),
                isRisky = false,
                details = context.getString(R.string.dns_no_network),
                description = context.getString(R.string.dns_desc),
                fixSuggestion = ""
            )
            val lp = cm.getLinkProperties(activeNetwork) ?: return ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.dns_check_title),
                isRisky = false,
                details = context.getString(R.string.dns_no_data),
                description = context.getString(R.string.dns_desc),
                fixSuggestion = ""
            )

            val dnsList = lp.dnsServers
            if (dnsList.isEmpty()) {
                return ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.dns_check_title),
                    isRisky = false,
                    details = context.getString(R.string.dns_no_servers),
                    description = context.getString(R.string.dns_desc),
                    fixSuggestion = ""
                )
            }

            val dnsStrings = dnsList.map { it.hostAddress ?: "" }.filter { it.isNotEmpty() }
            val customDnsFound = mutableListOf<String>()
            val publicDnsMap = mapOf(
                "8.8.8.8" to "Google Public DNS",
                "8.8.4.4" to "Google Public DNS",
                "1.1.1.1" to "Cloudflare DNS",
                "1.0.0.1" to "Cloudflare DNS",
                "9.9.9.9" to "Quad9 DNS",
                "149.112.112.112" to "Quad9 DNS",
                "208.67.222.222" to "OpenDNS",
                "208.67.220.220" to "OpenDNS",
                "94.140.14.14" to "AdGuard DNS",
                "94.140.15.15" to "AdGuard DNS",
                "77.88.8.8" to "Yandex.DNS",
                "77.88.8.1" to "Yandex.DNS",
                "77.88.8.2" to "Yandex.DNS",
                "4.2.2.1" to "Level3 DNS",
                "4.2.2.2" to "Level3 DNS",
                "4.2.2.3" to "Level3 DNS",
                "4.2.2.4" to "Level3 DNS"
            )

            for (dns in dnsStrings) {
                val cleanDns = dns.trim().split("%").first()
                if (publicDnsMap.containsKey(cleanDns)) {
                    customDnsFound.add("$cleanDns (${publicDnsMap[cleanDns]})")
                } else if (cleanDns.startsWith("2001:4860:4860::") || cleanDns.startsWith("2606:4700:4700::")) {
                    customDnsFound.add("$cleanDns (Public IPv6 DNS)")
                }
            }

            if (customDnsFound.isNotEmpty()) {
                val foundDetails = context.getString(R.string.dns_custom_detected, dnsStrings.joinToString(", "), customDnsFound.joinToString(", "))
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.dns_check_title),
                    isRisky = true,
                    details = foundDetails,
                    description = context.getString(R.string.dns_desc),
                    fixSuggestion = context.getString(R.string.dns_fix)
                )
            } else {
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.dns_check_title),
                    isRisky = false,
                    details = context.getString(R.string.dns_clean, dnsStrings.joinToString(", ")),
                    description = context.getString(R.string.dns_desc_clean),
                    fixSuggestion = ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.dns_check_title),
                isRisky = false,
                details = context.getString(R.string.direct_vpn_error, e.message ?: ""),
                description = "",
                fixSuggestion = ""
            )
        }
    }
}
