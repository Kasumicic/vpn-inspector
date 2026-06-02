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
        val lang = sharedPrefs.getString("app_lang", "ru") ?: "ru"
        return LocaleHelper.setLocale(getApplication(), lang)
    }

    init {
        val localContext = getLocalizedContext()
        _uiState = MutableStateFlow(ScannerUiState(ipAddress = localContext.getString(R.string.checking_val)))
        uiState = _uiState.asStateFlow()
        fetchIpOnly()
    }

    private fun fetchIpOnly() {
        viewModelScope.launch {
            val localContext = getLocalizedContext()
            val localScanner = VpnScanner(localContext)
            val (ip, _) = localScanner.getIpInfo()
            _uiState.value = _uiState.value.copy(ipAddress = ip)
        }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return

        val localContext = getLocalizedContext()
        val localScanner = VpnScanner(localContext)

        _uiState.value = _uiState.value.copy(
            isScanning = true,
            scanCompleted = false,
            results = emptyList(),
            decision = DecisionState.CLEAN,
            trustScore = 100,
            currentScanStatus = localContext.getString(R.string.scan_step_0)
        )

        viewModelScope.launch {
            val scanResults = mutableListOf<ScanResult>()
            
            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_1))
            val (ip, ipResults) = localScanner.getIpInfo()
            scanResults.addAll(ipResults)
            _uiState.value = _uiState.value.copy(ipAddress = ip, results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_2))
            scanResults.add(localScanner.checkIpv6Leak())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_3))
            scanResults.add(localScanner.checkDirectApi())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_4))
            scanResults.add(localScanner.checkSystemProxySettings())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_5))
            scanResults.add(localScanner.checkVpnApps())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_6))
            scanResults.add(localScanner.checkNotVpnCapability())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_7))
            scanResults.add(localScanner.checkInterfaces())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_8))
            scanResults.add(localScanner.checkMtuAnomalies())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_9))
            scanResults.add(localScanner.checkLocalProxies())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_10))
            scanResults.add(localScanner.checkFakeIp())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = localContext.getString(R.string.scan_step_11))
            scanResults.add(localScanner.analyzeLatency())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())
            
            // Calculate final verdict based on methodology table
            val hasGeoRisk = scanResults.any { it.category == ScanCategory.GEO && it.isRisky }
            val hasDirectRisk = scanResults.any { it.category == ScanCategory.DIRECT && it.isRisky }
            val hasIndirectRisk = scanResults.any { it.category == ScanCategory.INDIRECT && it.isRisky }

            val (decision, score) = when {
                !hasGeoRisk && !hasDirectRisk && !hasIndirectRisk -> DecisionState.CLEAN to 100
                !hasGeoRisk && hasDirectRisk && !hasIndirectRisk -> DecisionState.CLEAN to 80
                !hasGeoRisk && !hasDirectRisk && hasIndirectRisk -> DecisionState.CLEAN to 90
                hasGeoRisk && !hasDirectRisk && !hasIndirectRisk -> DecisionState.NEEDS_CHECK to 50
                !hasGeoRisk && hasDirectRisk && hasIndirectRisk -> DecisionState.NEEDS_CHECK to 40
                hasGeoRisk && hasDirectRisk && !hasIndirectRisk -> DecisionState.DETECTED to 10
                hasGeoRisk && !hasDirectRisk && hasIndirectRisk -> DecisionState.DETECTED to 10
                hasGeoRisk && hasDirectRisk && hasIndirectRisk -> DecisionState.DETECTED to 0
                else -> DecisionState.CLEAN to 100
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
