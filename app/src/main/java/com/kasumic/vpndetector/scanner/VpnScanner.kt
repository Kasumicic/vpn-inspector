package com.kasumic.vpndetector.scanner

import android.content.Context
import com.kasumic.vpndetector.scanner.checks.*

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

    suspend fun getIpInfo(): Pair<String, List<ScanResult>> {
        val check = GeoCheck(context)
        val geoResult = check.run()
        lastIpv4CountryCode = geoResult.countryCode
        return Pair(geoResult.ip, geoResult.results)
    }

    suspend fun checkIpv6Leak(): ScanResult {
        return Ipv6LeakCheck(context).run(lastIpv4CountryCode)
    }

    fun checkDirectApi(): ScanResult {
        return DirectApiCheck(context).run()
    }

    fun checkSystemProxySettings(): ScanResult {
        return ProxySettingsCheck(context).run()
    }

    fun checkNotVpnCapability(): ScanResult {
        return NotVpnCheck(context).run()
    }

    fun checkInterfaces(): ScanResult {
        return InterfacesCheck(context).run()
    }

    fun checkVpnApps(): ScanResult {
        return VpnAppsCheck(context).run()
    }

    fun checkMtuAnomalies(): ScanResult {
        return MtuCheck(context).run()
    }

    suspend fun checkLocalProxies(): ScanResult {
        return LocalProxyCheck(context).run()
    }

    suspend fun checkFakeIp(): ScanResult {
        return FakeIpCheck(context).run()
    }

    fun checkDnsServers(): ScanResult {
        return DnsServerCheck(context).run()
    }

    suspend fun analyzeLatency(): ScanResult {
        return LatencyCheck(context).run()
    }

    suspend fun checkDatacenter(ip: String?): ScanResult {
        return DatacenterCheck(context).run(ip)
    }
}
