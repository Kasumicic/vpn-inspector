package com.kasumic.vpndetector.scanner

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.kasumic.vpndetector.api.NetworkClient
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
    val fixSuggestion: String
)

class VpnScanner(private val context: Context) {

    suspend fun getIpInfo(): Pair<String, List<ScanResult>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ScanResult>()
        var ip = "Unknown"
        try {
            val response = NetworkClient.ipApiService.getIpInfo()
            if (response.status == "success") {
                ip = response.query ?: "Unknown"
                val proxyStr = if (response.proxy == true) "Да" else "Нет"
                val hostingStr = if (response.hosting == true) "Да" else "Нет"
                val countryCode = response.countryCode ?: "Неизвестно"
                val notRussia = countryCode != "RU"

                val isRisky = (response.proxy == true) || (response.hosting == true) || notRussia
                results.add(
                    ScanResult(
                        category = ScanCategory.GEO,
                        moduleName = "Анализ IP (GeoIP)",
                        isRisky = isRisky,
                        details = if (isRisky) "Подозрительный IP:\nСтрана: $countryCode\nProxy: $proxyStr\nХостинг: $hostingStr" else "Чистый IP (Ожидаемый регион)",
                        description = "Метод определяет использование VPN на стороне сервера, сравнивая IP с репутационными базами (GeoIP).",
                        fixSuggestion = if (isRisky) "Используйте резидентные прокси или настройте маршрутизацию (Split Tunneling) на российские IP адреса, чтобы локальный трафик шел напрямую, минуя проверку GeoIP." else "Обход надежно скрыт на уровне GeoIP."
                    )
                )
            } else {
                results.add(ScanResult(ScanCategory.GEO, "Анализ IP", false, "Ошибка сети", "Не удалось получить GeoIP.", ""))
            }
        } catch (e: Exception) {
            results.add(ScanResult(ScanCategory.GEO, "Анализ IP", false, "Ошибка: ${e.message}", "Не удалось подключиться API.", ""))
        }
        return@withContext ip to results
    }

    fun checkDirectApi(): ScanResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return ScanResult(ScanCategory.DIRECT, "Системный VPN API", false, "Нет активной сети", "Проверка системного флага VPN.", "")
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ScanResult(ScanCategory.DIRECT, "Системный VPN API", false, "Нет данных", "Проверка системного флага VPN.", "")

            val hasVpnTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            val isNotVpnCap = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

            if (hasVpnTransport || !isNotVpnCap) {
                ScanResult(ScanCategory.DIRECT, "Системный VPN API", true, "ОБНАРУЖЕН VPN (NetworkCapabilities)", "Система напрямую сообщает о наличии активного VPN-туннеля.", "Используйте Proxy-режим (в обход VpnService) или VPN-клиенты с root-правами (например, iptables) для прозрачного перенаправления без системного флага.")
            } else {
                ScanResult(ScanCategory.DIRECT, "Системный VPN API", false, "ЧИСТО", "Проверка прямого системного флага VPN.","")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.DIRECT, "Системный VPN API", false, "Ошибка: ${e.message}", "Ошибка доступа.","")
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
                ScanResult(ScanCategory.INDIRECT, "Сетевые интерфейсы", true, "НАЙДЕНЫ (${badIfaces.joinToString(", ")})", "Косвенный признак. Наличие виртуальных интерфейсов (tun/tap/wg) может указывать на туннель.", "Отмените создание tun-интерфейса в настройках клиента (используйте чистый socks/http proxy), либо переименуйте интерфейс через root-доступ.")
            } else {
                ScanResult(ScanCategory.INDIRECT, "Сетевые интерфейсы", false, "ЧИСТО", "Косвенная проверка на наличие виртуальных адаптеров туннелирования.", "")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.INDIRECT, "Сетевые интерфейсы", false, "Ошибка: ${e.message}", "Ошибка чтения интерфейсов.", "")
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
            "io.nekohasekai.sagerenet" to "Matsuri/NekoBox"
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
            ScanResult(ScanCategory.DIRECT, "Пакеты приложений", true, "НАЙДЕНЫ (${foundApps.joinToString(", ")})", "Прямой признак. Обнаружены установленные популярные VPN-клиенты.", "Используйте кастомные сборки/форки клиентов или скрывайте приложения через модули Magisk / App Hider.")
        } else {
            ScanResult(ScanCategory.DIRECT, "Пакеты приложений", false, "ЧИСТО", "Проверка на наличие популярных установленных VPN-клиентов.", "")
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
                        if (mtu in 1..1449) anomalies.add("$name (размер $mtu)")
                    } else if (name.contains("rmnet")) {
                        if (mtu in 1..1349) anomalies.add("$name (размер $mtu)")
                    } else if (mtu in 1..1399) {
                        anomalies.add("$name (размер $mtu)")
                    }
                }
            }
            if (anomalies.isNotEmpty()) {
                ScanResult(ScanCategory.INDIRECT, "Аномалии MTU", true, "ПОНИЖЕН: ${anomalies.joinToString(", ")}", "Косвенный признак. Пониженный MTU характерен для туннелированного трафика из-за инкапсуляции заголовков.", "Отрегулируйте MTU виртуального интерфейса до стандартных значений (1420-1500) в расширенных настройках VPN-клиента.")
            } else {
                ScanResult(ScanCategory.INDIRECT, "Аномалии MTU", false, "ЧИСТО", "Анализ размера кадра (MTU) на косвенные следы инкапсуляции VPN.", "")
            }
        } catch (e: Exception) {
            ScanResult(ScanCategory.INDIRECT, "Аномалии MTU", false, "Ошибка: ${e.message}", "", "")
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
            ScanResult(ScanCategory.INDIRECT, "Локальные Прокси", true, "ОТКРЫТЫ ПОРТЫ (${foundPorts.joinToString(", ")})", "Косвенный вызов. Открытые порты могут указывать на работу локального Proxy/Xray/Socks5 сервера.", "Смените стандартные порты входящего подключения (1080, 10808) на случайные динамические порты (например, 49211) в конфигурации клиента.")
        } else {
            ScanResult(ScanCategory.INDIRECT, "Локальные Прокси", false, "ЧИСТО", "Анализ открытых локальных портов, характерных для proxy-клиентов.", "")
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
            ScanResult(ScanCategory.INDIRECT, "Подмена Fake-IP", true, "ОБНАРУЖЕНО (${foundFakeIps.joinToString(", ")})", "Косвенный признак. VPN-ядро (например, Xray/sing-box) подменяет IP для перехвата трафика.", "Отключите функцию Fake-IP в конфигурации DNS (переключитесь на Real-IP, redir-host или remote DNS).")
        } else {
            ScanResult(ScanCategory.INDIRECT, "Подмена Fake-IP", false, "ЧИСТО", "Проверка на выдачу фейковых внутренних IP-адресов популярным серверам (Fake-IP туннелирование).", "")
        }
    }
}
