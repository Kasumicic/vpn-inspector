package com.kasumic.vpndetector.scanner.checks

import android.content.Context
import android.os.Build
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.ScanResult

class VpnAppsCheck(private val context: Context) {
    fun run(): ScanResult {
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
            ScanResult(
                category = ScanCategory.DIRECT,
                moduleName = context.getString(R.string.app_packages_title),
                isRisky = true,
                details = context.getString(R.string.app_packages_found, foundApps.joinToString(", ")),
                description = context.getString(R.string.app_packages_desc),
                fixSuggestion = context.getString(R.string.app_packages_fix)
            )
        } else {
            ScanResult(
                category = ScanCategory.DIRECT,
                moduleName = context.getString(R.string.app_packages_title),
                isRisky = false,
                details = context.getString(R.string.app_packages_clean),
                description = context.getString(R.string.app_packages_desc_clean),
                fixSuggestion = ""
            )
        }
    }
}
