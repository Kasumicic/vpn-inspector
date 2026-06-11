package com.kasumic.vpndetector

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kasumic.vpndetector.scanner.ScanResult
import com.kasumic.vpndetector.scanner.ScanCategory
import com.kasumic.vpndetector.scanner.VpnScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DecisionState { CLEAN, NEEDS_CHECK, DETECTED }

data class ScannerUiState(
    val ipAddress: String = "",
    val isScanning: Boolean = false,
    val currentScanStatus: String = "",
    val results: List<ScanResult> = emptyList(),
    val scanCompleted: Boolean = false,
    val decision: DecisionState = DecisionState.CLEAN,
    val trustScore: Int = 100
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState: MutableStateFlow<ScannerUiState>
    val uiState: StateFlow<ScannerUiState>

    private fun getLocalizedContext(): Context {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val systemLanguage = java.util.Locale.getDefault().language
        val defaultLang = if (systemLanguage == "ru") "ru" else "en"
        val lang = sharedPrefs.getString("app_lang", defaultLang) ?: defaultLang
        return LocaleHelper.setLocale(getApplication(), lang)
    }

    init {
        _uiState = MutableStateFlow(ScannerUiState(ipAddress = "—"))
        uiState = _uiState.asStateFlow()
    }

    fun startScan() {
        if (_uiState.value.isScanning) return

        val localContext = getLocalizedContext()
        val localScanner = VpnScanner(localContext)

        _uiState.value = _uiState.value.copy(
            isScanning = true,
            scanCompleted = false,
            ipAddress = "—",
            results = emptyList(),
            decision = DecisionState.CLEAN,
            trustScore = 100,
            currentScanStatus = localContext.getString(R.string.scan_step_0)
        )

        viewModelScope.launch {
            val scanResults = mutableListOf<ScanResult>()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

             // Dynamic check enabled mapping using our central AppConfig list
             val enabledChecks = AppConfig.ALL_CHECKS.associate { module ->
                 module.key to sharedPrefs.getBoolean(module.key, module.defaultValue)
             }
 
             val isGeoEnabled = enabledChecks["check_geo"] == true
             val isIpv6LeakEnabled = enabledChecks["check_ipv6_leak"] == true
             val isDirectApiEnabled = enabledChecks["check_direct_api"] == true
             val isProxySettingsEnabled = enabledChecks["check_system_proxy"] == true
             val isVpnAppsEnabled = enabledChecks["check_vpn_apps"] == true
             val isNotVpnEnabled = enabledChecks["check_not_vpn_capability"] == true
             val isInterfacesEnabled = enabledChecks["check_interfaces"] == true
             val isMtuEnabled = enabledChecks["check_mtu_anomalies"] == true
             val isLocalProxiesEnabled = enabledChecks["check_local_proxies"] == true
             val isFakeIpEnabled = enabledChecks["check_fake_ip"] == true
             val isDnsEnabled = enabledChecks["check_dns_servers"] == true
             val isLatencyEnabled = enabledChecks["check_latency"] == true
             val isDatacenterEnabled = enabledChecks["check_datacenter"] == true
 
             val totalActiveSteps = enabledChecks.values.count { it }

            var currentStepIndex = 0

            fun getStatusText(stringResId: Int, index: Int): String {
                val rawString = localContext.getString(stringResId)
                return rawString.replace(Regex("\\(\\d+/\\d+\\)"), "($index/$totalActiveSteps)")
            }

            _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_0, 0))

            var ip = "—"

            if (isGeoEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_1, currentStepIndex))
                val (fetchedIp, ipResults) = localScanner.getIpInfo()
                ip = fetchedIp
                scanResults.addAll(ipResults)
                _uiState.value = _uiState.value.copy(ipAddress = ip, results = scanResults.toList())
            }

            if (isDatacenterEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_13, currentStepIndex))
                scanResults.add(localScanner.checkDatacenter(ip))
                val currentDetected = localScanner.detectedIp
                if (currentDetected != "—") {
                    ip = currentDetected
                }
                _uiState.value = _uiState.value.copy(ipAddress = ip, results = scanResults.toList())
            }

            if (isIpv6LeakEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_2, currentStepIndex))
                scanResults.add(localScanner.checkIpv6Leak())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isDirectApiEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_3, currentStepIndex))
                scanResults.add(localScanner.checkDirectApi())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isProxySettingsEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_4, currentStepIndex))
                scanResults.add(localScanner.checkSystemProxySettings())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isVpnAppsEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_5, currentStepIndex))
                scanResults.add(localScanner.checkVpnApps())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isNotVpnEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_6, currentStepIndex))
                scanResults.add(localScanner.checkNotVpnCapability())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isInterfacesEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_7, currentStepIndex))
                scanResults.add(localScanner.checkInterfaces())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isMtuEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_8, currentStepIndex))
                scanResults.add(localScanner.checkMtuAnomalies())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isLocalProxiesEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_9, currentStepIndex))
                scanResults.add(localScanner.checkLocalProxies())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isFakeIpEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_10, currentStepIndex))
                scanResults.add(localScanner.checkFakeIp())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isDnsEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_11, currentStepIndex))
                scanResults.add(localScanner.checkDnsServers())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }

            if (isLatencyEnabled) {
                currentStepIndex++
                _uiState.value = _uiState.value.copy(currentScanStatus = getStatusText(R.string.scan_step_12, currentStepIndex))
                scanResults.add(localScanner.analyzeLatency())
                _uiState.value = _uiState.value.copy(results = scanResults.toList())
            }
            
            // Calculate dynamic trust score based on individual check weights
            var penaltySum = 0
            val localContext = getLocalizedContext()
            
            scanResults.forEach { result ->
                if (result.isRisky && !result.isError) {
                    when (result.category) {
                        ScanCategory.GEO -> {
                            penaltySum += 45 // GeoIP mismatch is highly suspicious
                        }
                        ScanCategory.DIRECT -> {
                            if (result.moduleName == localContext.getString(R.string.app_packages_title)) {
                                penaltySum += 10 // Installed clients package scanner
                            } else {
                                penaltySum += 30 // Direct VPN transports or HTTP/SOCKS system proxy
                            }
                        }
                        ScanCategory.INDIRECT -> {
                            when (result.moduleName) {
                                localContext.getString(R.string.interfaces_title) -> penaltySum += 20 // active virtual ifaces (tun/tap/wg)
                                localContext.getString(R.string.not_vpn_title) -> penaltySum += 25 // missing NOT_VPN interface capability
                                localContext.getString(R.string.fake_ip_title) -> penaltySum += 25 // Fake-IP internal addresses (DNS Spoof)
                                localContext.getString(R.string.snitch_title) -> penaltySum += 25 // Snitch interception latency anomalies
                                localContext.getString(R.string.local_proxy_title) -> penaltySum += 15 // Active local proxy ports (1080, 10808)
                                localContext.getString(R.string.mtu_anomalies_title) -> penaltySum += 10 // Reduced MTU packet size
                                localContext.getString(R.string.dns_check_title) -> penaltySum += 15 // Off-provider public/private DNS
                                localContext.getString(R.string.ipv6_leak_title) -> penaltySum += 15 // IPv6 leakage / split routing mismatch
                                localContext.getString(R.string.datacenter_check_title) -> penaltySum += 30 // Datacenter / hosting IP
                                else -> penaltySum += 15
                            }
                        }
                    }
                }
            }

            val hasGeoRisk = scanResults.any { it.category == ScanCategory.GEO && it.isRisky }
            val hasDirectRisk = scanResults.any { it.category == ScanCategory.DIRECT && it.isRisky }
            val hasIndirectRisk = scanResults.any { it.category == ScanCategory.INDIRECT && it.isRisky }

            val decision = when {
                !hasGeoRisk && !hasDirectRisk && !hasIndirectRisk -> DecisionState.CLEAN // Row 1
                !hasGeoRisk && hasDirectRisk && !hasIndirectRisk -> DecisionState.CLEAN  // Row 2
                !hasGeoRisk && !hasDirectRisk && hasIndirectRisk -> DecisionState.CLEAN  // Row 3
                hasGeoRisk && !hasDirectRisk && !hasIndirectRisk -> DecisionState.NEEDS_CHECK // Row 4
                !hasGeoRisk && hasDirectRisk && hasIndirectRisk -> DecisionState.NEEDS_CHECK  // Row 5
                hasGeoRisk && hasDirectRisk && !hasIndirectRisk -> DecisionState.DETECTED     // Row 6
                hasGeoRisk && !hasDirectRisk && hasIndirectRisk -> DecisionState.DETECTED     // Row 7
                hasGeoRisk && hasDirectRisk && hasIndirectRisk -> DecisionState.DETECTED      // Row 8
                else -> DecisionState.CLEAN
            }

            val score = when (decision) {
                DecisionState.CLEAN -> {
                    (100 - penaltySum).coerceIn(80, 100)
                }
                DecisionState.NEEDS_CHECK -> {
                    (100 - penaltySum).coerceIn(35, 75)
                }
                DecisionState.DETECTED -> {
                    (100 - penaltySum).coerceIn(0, 30)
                }
            }
            
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scanCompleted = true,
                currentScanStatus = "",
                results = scanResults.toList(),
                decision = decision,
                trustScore = score
            )
        }
    }
}
