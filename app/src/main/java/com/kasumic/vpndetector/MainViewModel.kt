package com.kasumic.vpndetector

import android.app.Application
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
    val ipAddress: String = "Проверка...",
    val isScanning: Boolean = false,
    val currentScanStatus: String = "",
    val results: List<ScanResult> = emptyList(),
    val scanCompleted: Boolean = false,
    val decision: DecisionState = DecisionState.CLEAN,
    val trustScore: Int = 100
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = VpnScanner(application)
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        fetchIpOnly()
    }

    private fun fetchIpOnly() {
        viewModelScope.launch {
            val (ip, _) = scanner.getIpInfo()
            _uiState.value = _uiState.value.copy(ipAddress = ip)
        }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return

        _uiState.value = _uiState.value.copy(
            isScanning = true,
            scanCompleted = false,
            results = emptyList(),
            decision = DecisionState.CLEAN,
            trustScore = 100,
            currentScanStatus = "Настройка сканирования (0/8)"
        )

        viewModelScope.launch {
            val scanResults = mutableListOf<ScanResult>()
            
            _uiState.value = _uiState.value.copy(currentScanStatus = "Анализ IP и GeoIP (1/8)...")
            val (ip, ipResults) = scanner.getIpInfo()
            scanResults.addAll(ipResults)
            _uiState.value = _uiState.value.copy(ipAddress = ip, results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка системного VPN (2/8)...")
            scanResults.add(scanner.checkDirectApi())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка интерфейсов (3/8)...")
            scanResults.add(scanner.checkInterfaces())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка пакетов (4/8)...")
            scanResults.add(scanner.checkVpnApps())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка MTU (5/8)...")
            scanResults.add(scanner.checkMtuAnomalies())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка локального прокси (6/8)...")
            scanResults.add(scanner.checkLocalProxies())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Проверка Fake-IP (7/8)...")
            scanResults.add(scanner.checkFakeIp())
            _uiState.value = _uiState.value.copy(results = scanResults.toList())

            _uiState.value = _uiState.value.copy(currentScanStatus = "Анализ сетевой задержки (8/8)...")
            scanResults.add(scanner.analyzeLatency())
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
