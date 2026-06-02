package com.kasumic.vpndetector.scanner

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.kasumic.vpndetector.api.NetworkClient
import com.kasumic.vpndetector.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.NetworkInterface
import java.net.Socket
import java.net.InetAddress

enum class ScanCategory { GEO, DIRECT, INDIRECT }

data class ScanResult(
    val category: ScanCategory,
    val moduleName: String,
    val isRisky: Boolean,
    val details: String,
    val description: String,
    val fixSuggestion: String,
    val isError: Boolean = false
)

class VpnScanner(private val context: Context) {
    private var lastIpv4CountryCode: String? = null

    suspend fun getIpInfo(): Pair<String, List<ScanResult>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ScanResult>()
        var ip = context.getString(R.string.unknown_val)
        try {
            val response = NetworkClient.ipApiService.getIpInfo()
            if (response.ip != null) {
                ip = response.ip
                val countryCode = response.country?.iso ?: context.getString(R.string.unknown_val)
                lastIpv4CountryCode = countryCode
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val targetRegion = sharedPrefs.getString("target_region", "RU") ?: "RU"
                val notTarget = !countryCode.equals(targetRegion, ignoreCase = true)

                val isRisky = notTarget
                results.add(
                    ScanResult(
                        category = ScanCategory.GEO,
                        moduleName = context.getString(R.string.geo_analysis_title),
                        isRisky = isRisky,
                        details = if (isRisky) {
                            context.getString(R.string.geo_detail_suspicious, countryCode)
                        } else {
                            context.getString(R.string.geo_detail_clean, countryCode)
                        },
                        description = context.getString(R.string.geo_desc),
                        fixSuggestion = if (isRisky) {
                            context.getString(R.string.geo_fix_suspicious)
                        } else {
                            context.getString(R.string.geo_fix_clean)
                        }
                    )
                )
            } else {
                results.add(ScanResult(
                    category = ScanCategory.GEO,
                    moduleName = context.getString(R.string.geo_analysis_title),
                    isRisky = false,
                    details = context.getString(R.string.geo_error_network),
                    description = context.getString(R.string.geo_error_server),
                    fixSuggestion = "",
                    isError = true
                ))
            }
        } catch (e: Exception) {
            try {
                val request = okhttp3.Request.Builder().url("https://api64.ipify.org").build()
                val response = NetworkClient.okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    ip = response.body?.string()?.trim() ?: ip
                }
            } catch (fallbackE: Exception) {
                // Ignore fallback exception
            }
            results.add(ScanResult(
                category = ScanCategory.GEO,
                moduleName = context.getString(R.string.geo_analysis_title),
                isRisky = false,
                details = context.getString(R.string.geo_error_prefix, e.message ?: ""),
                description = context.getString(R.string.geo_error_connect),
                fixSuggestion = "",
                isError = true
            ))
        }
        return@withContext ip to results
    }

    fun checkDirectApi(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(ScanCategory.DIRECT, context.getString(R.string.direct_vpn_api_title), false, context.getString(R.string.direct_vpn_no_network), context.getString(R.string.direct_vpn_desc), "")
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ScanResult(ScanCategory.DIRECT, context.getString(R.string.direct_vpn_api_title), false, context.getString(R.string.direct_vpn_no_data), context.getString(R.string.direct_vpn_desc), "")

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
                ScanResult(ScanCategory.DIRECT, context.getString(R.string.direct_vpn_api_title), true, context.getString(R.string.direct_vpn_found, detailDesc), context.getString(R.string.direct_vpn_found_desc), context.getString(R.string.direct_vpn_fix))
            } else {
                ScanResult(ScanCategory.DIRECT, context.getString(R.string.direct_vpn_api_title), false, context.getString(R.string.direct_vpn_clean), context.getString(R.string.direct_vpn_desc),"")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.DIRECT, context.getString(R.string.direct_vpn_api_title), false, context.getString(R.string.direct_vpn_error, e.message ?: ""), "","")
        }
    }

    fun checkSystemProxySettings(): ScanResult {
        return try {
            val host = System.getProperty("http.proxyHost") ?: System.getProperty("https.proxyHost") ?: System.getProperty("socksProxyHost")
            val port = System.getProperty("http.proxyPort") ?: System.getProperty("https.proxyPort") ?: System.getProperty("socksProxyPort")
            
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
            ScanResult(ScanCategory.DIRECT, context.getString(R.string.proxy_settings_title), false, context.getString(R.string.direct_vpn_error, e.message ?: ""), "", "")
        }
    }

    fun checkNotVpnCapability(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(ScanCategory.INDIRECT, context.getString(R.string.not_vpn_title), false, context.getString(R.string.not_vpn_no_network), context.getString(R.string.not_vpn_desc), "")
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ScanResult(ScanCategory.INDIRECT, context.getString(R.string.not_vpn_title), false, context.getString(R.string.not_vpn_no_data), context.getString(R.string.not_vpn_desc), "")

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
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.not_vpn_title), false, context.getString(R.string.direct_vpn_error, e.message ?: ""), "", "")
        }
    }

    fun checkInterfaces(): ScanResult {
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
                ScanResult(ScanCategory.INDIRECT, context.getString(R.string.interfaces_title), true, context.getString(R.string.interfaces_found, badIfaces.joinToString(", ")), context.getString(R.string.interfaces_desc), context.getString(R.string.interfaces_fix))
            } else {
                ScanResult(ScanCategory.INDIRECT, context.getString(R.string.interfaces_title), false, context.getString(R.string.interfaces_clean), context.getString(R.string.interfaces_desc_clean), "")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.interfaces_title), false, context.getString(R.string.direct_vpn_error, e.message ?: ""), context.getString(R.string.interfaces_error), "")
        }
    }

    fun checkVpnApps(): ScanResult {
        val knownVpns = mapOf(
            "com.wireguard.android" to "WireGuard",
            "ch.protonvpn.android" to "ProtonVPN",
            "com.cloudflare.onedotonedotonedotone" to "1.1.1.1 (WARP)",
            "org.thunderbird.vpn" to "Mozilla VPN",
            "com.v2ray.ang" to "v2rayNG",
            "com.happproxy" to "Happ Proxy",
            "com.v2raytun.android" to "V2Ray Tun",
            "com.nekohasekai.sagerenet" to "SagerNet",
            "com.v2raytun" to "V2Ray Tun",
            "com.nebula.karing" to "Karing",
            "com.github.dyhkwong.sagernet" to "Exclave",
            "com.follow.clashx" to "FClashX",
            "moe.nb4a" to "NekoBox",
            "com.github.kr328.clash" to "Clash for Android",
            "com.github.shadowsocks" to "Shadowsocks",
            "de.blinkt.openvpn" to "OpenVPN for Android",
            "net.openvpn.openvpn" to "OpenVPN Connect",
            "com.surfshark.vpnclient.android" to "Surfshark",
            "com.nordvpn.android" to "NordVPN",
            "com.expressvpn.vpn" to "ExpressVPN",
            "com.psiphon3.subscriptions" to "Psiphon Pro",
            "org.outline.android.client" to "Outline",
            "io.nekohasekai.sagerenet" to "Matsuri/NekoBox",
            "io.github.romanvht.byedpi" to "ByeDPI",
            "org.amnezia.vpn" to "AmneziaVPN",
            "com.tunnelbear.android" to "TunnelBear",
            "com.windscribe.vpn" to "Windscribe",
            "org.torproject.torbrowser" to "Tor Browser",
            "org.torproject.android" to "Orbot (Tor)",
            "com.antizabor" to "АнтиЗабор",
            "org.zaborona.vpn" to "Zaborona VPN"
        )
        val foundApps = mutableListOf<String>()
        val pm = context.packageManager
        for ((pkg, appName) in knownVpns) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
                foundApps.add(appName)
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                // Not installed
            }
        }
        return if (foundApps.isNotEmpty()) {
            ScanResult(ScanCategory.DIRECT, context.getString(R.string.app_packages_title), true, context.getString(R.string.app_packages_found, foundApps.joinToString(", ")), context.getString(R.string.app_packages_desc), context.getString(R.string.app_packages_fix))
        } else {
            ScanResult(ScanCategory.DIRECT, context.getString(R.string.app_packages_title), false, context.getString(R.string.app_packages_clean), context.getString(R.string.app_packages_desc_clean), "")
        }
    }

    fun checkMtuAnomalies(): ScanResult {
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
                ScanResult(ScanCategory.INDIRECT, context.getString(R.string.mtu_anomalies_title), true, context.getString(R.string.mtu_low, anomalies.joinToString(", ")), context.getString(R.string.mtu_desc), context.getString(R.string.mtu_fix))
            } else {
                ScanResult(ScanCategory.INDIRECT, context.getString(R.string.mtu_anomalies_title), false, context.getString(R.string.mtu_clean), context.getString(R.string.mtu_desc_clean), "")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.mtu_anomalies_title), false, context.getString(R.string.mtu_error, e.message ?: ""), "", "")
        }
    }

    suspend fun checkLocalProxies(): ScanResult = withContext(Dispatchers.IO) {
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
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.local_proxy_title), true, context.getString(R.string.local_proxy_open, foundPorts.joinToString(", ")), context.getString(R.string.local_proxy_desc), context.getString(R.string.local_proxy_fix))
        } else {
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.local_proxy_title), false, context.getString(R.string.local_proxy_clean), context.getString(R.string.local_proxy_desc_clean), "")
        }
    }

    suspend fun checkFakeIp(): ScanResult = withContext(Dispatchers.IO) {
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
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.fake_ip_title), true, context.getString(R.string.fake_ip_detected, foundFakeIps.joinToString(", ")), context.getString(R.string.fake_ip_desc), context.getString(R.string.fake_ip_fix))
        } else {
            ScanResult(ScanCategory.INDIRECT, context.getString(R.string.fake_ip_title), false, context.getString(R.string.fake_ip_clean), context.getString(R.string.fake_ip_desc_clean), "")
        }
    }

    suspend fun analyzeLatency(): ScanResult = withContext(Dispatchers.IO) {
        fun getTcpPing(host: String, port: Int = 80): Int {
            return try {
                val address = InetAddress.getByName(host)
                // Используем nanoTime для точности как perf_counter()
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

        val pingRu = getTcpPing("ya.ru")
        val pingEu = getTcpPing("google.com")

        if (pingRu >= 999 && pingEu >= 999) {
            return@withContext ScanResult(ScanCategory.INDIRECT, context.getString(R.string.snitch_title), false, context.getString(R.string.snitch_timeout), context.getString(R.string.snitch_desc_rtt), context.getString(R.string.snitch_timeout_fix))
        }

        // Если пинг подозрительно мал (0-2мс), значит это перехват прокси (localhost)
        if (pingRu <= 2 || pingEu <= 2) {
            return@withContext ScanResult(ScanCategory.INDIRECT, context.getString(R.string.snitch_title), true, context.getString(R.string.snitch_intercept, pingRu.toString(), pingEu.toString()), context.getString(R.string.snitch_intercept_desc), context.getString(R.string.snitch_intercept_fix))
        }

        val ratio = pingRu.toFloat() / if (pingEu > 0) pingEu else 1
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val targetRegion = sharedPrefs.getString("target_region", "RU") ?: "RU"
        val isRuRegion = targetRegion.equals("RU", ignoreCase = true)

        if (isRuRegion && ratio > 3f && pingRu > 100) {
            return@withContext ScanResult(ScanCategory.INDIRECT, context.getString(R.string.snitch_title), true, context.getString(R.string.snitch_anomaly, pingRu.toString(), pingEu.toString()), context.getString(R.string.snitch_anomaly_desc), context.getString(R.string.snitch_anomaly_fix))
        }

        ScanResult(ScanCategory.INDIRECT, context.getString(R.string.snitch_title), false, context.getString(R.string.snitch_clean, pingRu.toString(), pingEu.toString()), context.getString(R.string.snitch_desc_rtt_clean), "")
    }

    private fun fetchBackupGeoCountry(ip: String): String? {
        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipapi.co/$ip/json/")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"country_code\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipwho.is/$ip")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"country_code\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val request = okhttp3.Request.Builder()
                .url("https://freeipapi.com/api/json/$ip")
                .build()
            NetworkClient.okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val match = java.util.regex.Pattern.compile("\"countryCode\"\\s*:\\s*\"([^\"]+)\"").matcher(body)
                        if (match.find()) {
                            return match.group(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return null
    }

    suspend fun checkIpv6Leak(): ScanResult = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder()
                .url("https://api6.ipify.org")
                .build()
            val response = NetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val ipv6 = response.body?.string()?.trim()
                if (!ipv6.isNullOrEmpty()) {
                    var ipv6Country: String? = null
                    try {
                        val geoResponse = NetworkClient.ipApiService.getIpInfoFor(ipv6)
                        ipv6Country = geoResponse.country?.iso
                    } catch (e: Exception) {
                        // ignore and try fallback
                    }

                    if (ipv6Country.isNullOrEmpty() || ipv6Country == "null") {
                        ipv6Country = fetchBackupGeoCountry(ipv6)
                    }

                    val finalIpv6Country = ipv6Country ?: context.getString(R.string.unknown_val)
                    val rsharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val rtargetRegion = rsharedPrefs.getString("target_region", "RU") ?: "RU"
                    val ipv4Country = lastIpv4CountryCode ?: rtargetRegion

                    val isUnknownIpv4 = ipv4Country == "Неизвестно" || ipv4Country == "Unknown" || ipv4Country == context.getString(R.string.unknown_val)
                    val isUnknownIpv6 = finalIpv6Country == "Неизвестно" || finalIpv6Country == "Unknown" || finalIpv6Country == context.getString(R.string.unknown_val)

                    val isLeak = if (!isUnknownIpv4 && !isUnknownIpv6) {
                        ipv4Country != finalIpv6Country
                    } else {
                        false
                    }

                    if (isLeak) {
                        ScanResult(
                            category = ScanCategory.INDIRECT,
                            moduleName = context.getString(R.string.ipv6_leak_title),
                            isRisky = true,
                            details = context.getString(R.string.ipv6_leak_detected, ipv4Country, ipv6, finalIpv6Country),
                            description = context.getString(R.string.ipv6_leak_desc),
                            fixSuggestion = context.getString(R.string.ipv6_leak_fix)
                        )
                    } else {
                        val detailsText = if (!isUnknownIpv6 && !isUnknownIpv4) {
                            context.getString(R.string.ipv6_leak_safe, ipv6, finalIpv6Country, ipv4Country)
                        } else if (!isUnknownIpv6) {
                            context.getString(R.string.ipv6_leak_clean_active, ipv6, finalIpv6Country)
                        } else {
                            context.getString(R.string.ipv6_leak_clean_unknown, ipv6)
                        }
                        ScanResult(
                            category = ScanCategory.INDIRECT,
                            moduleName = context.getString(R.string.ipv6_leak_title),
                            isRisky = false,
                            details = detailsText,
                            description = context.getString(R.string.ipv6_leak_desc_clean),
                            fixSuggestion = ""
                        )
                    }
                } else {
                    ScanResult(
                        category = ScanCategory.INDIRECT,
                        moduleName = context.getString(R.string.ipv6_leak_title),
                        isRisky = false,
                        details = context.getString(R.string.ipv6_leak_clean_no_addr),
                        description = context.getString(R.string.ipv6_leak_desc_clean),
                        fixSuggestion = ""
                    )
                }
            } else {
                ScanResult(
                    category = ScanCategory.INDIRECT,
                    moduleName = context.getString(R.string.ipv6_leak_title),
                    isRisky = false,
                    details = context.getString(R.string.ipv6_leak_clean_host_unreachable),
                    description = context.getString(R.string.ipv6_leak_desc_clean),
                    fixSuggestion = ""
                )
            }
        } catch (e: Exception) {
            ScanResult(
                category = ScanCategory.INDIRECT,
                moduleName = context.getString(R.string.ipv6_leak_title),
                isRisky = false,
                details = context.getString(R.string.ipv6_leak_clean_not_supported),
                description = context.getString(R.string.ipv6_leak_desc_clean),
                fixSuggestion = ""
            )
        }
    }
}
