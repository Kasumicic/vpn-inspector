<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="150" height="150" alt="VPN Inspector Logo"/>
  
  <h1>🛡️ VPN Inspector</h1>
  <p>
    <b>Bypass Analyzer: Demonstration of VPN and Proxy detection methods on mobile devices.</b>
  </p>

  <p>
    <a href="README_RU.md">🇷🇺 Прочитать на русском языке</a>
  </p>

  <p>
    <a href="https://android.com"><img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" /></a>
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
    <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Compose" />
    <img src="https://img.shields.io/badge/Retrofit-FF5722?style=for-the-badge&logo=appveyor&logoColor=white" alt="Retrofit" />
  </p>
</div>

---

## 📸 Screenshots

To see the application in action, refer to the screenshots below. 

### English Interface
<div align="center">
  <img src="docs/screenshots/main_en.png" width="300" alt="VPN Inspector Main Screen"/>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/screenshots/settings_en.png" width="300" alt="VPN Inspector Settings Screen"/>
</div>

---

## 📖 About the Project

**VPN Inspector** is a premium Android utility designed to demonstrate and closely analyze the mechanisms of VPN and Proxy detection. It implements real-world scanning checks based on officially approved methodologies for detecting bypass tools.

The primary objective of the application is to show how different systems can identify when communication is tunneled, proving that comprehensive checks are necessary to properly detect bypass tools.

## 🎯 Target Audience

* 👨‍💻 **Developers and Security Researchers** interested in network isolation, tunneling mechanisms, and mobile security.
* 🔐 **Privacy Enthusiasts** wishing to evaluate the integrity of their current VPN service or Proxy configurations.
* 🎓 **Students and Academics** examining low-level network interface properties on Android OS.

## 🔬 Implemented Verification Suite

The application runs a complete, multi-step detection suite composed of 11 distinct checks:

1. **IP & GeoIP Analysis (Server-side)**: Resolves the public IP address and matches it against database markers (identifies data centers, hosting providers, CDNs, and region mismatches).
2. **Real IPv6 Leak Detector**: Actively queries a secure IPv6-only host to determine if IPv6 traffic bypasses the active VPN tunnel while IPv4 is tunneled.
3. **ConnectivityManager VPN Flag**: Directly queries the system network capabilities for an active `TRANSPORT_VPN` adapter.
4. **System-level Proxies**: Reads JVM configuration properties (such as `System.getProperty("http.proxyHost")`) to check for manual system proxification.
5. **Known VPN Packages Scanning**: Safely inspects the device package collection for active or installed bypass applications (e.g., ByeDPI, AmneziaVPN, WireGuard, ShadowSocks, Tor).
6. **Capabilities NOT_VPN Flag**: Checks for the absence of the custom `NET_CAPABILITY_NOT_VPN` capability flag on the primary internet transport.
7. **Virtual Interface Detection**: Parses the Linux network bindings looking for virtual adaptors associated with tunneling protocols (such as `tun0`, `tap0`, `wg0`, `ppp0`).
8. **MTU Size Analysis**: Calculates maximum transmission unit dimensions. Artificially low packet capacities are historically linked to packet encapsulation from VPN wrapping.
9. **Active Local Proxy Ports**: Assesses open loopback sockets (`127.0.0.1`) corresponding to local traffic redirectors on common port frequencies.
10. **Sandbox Fake-IP Detection**: Dispatches custom resolved lookups to check if spoofing nodes are outputting synthetic subnets.
11. **SNITCH Network Latency Anomaly**: Measures physical route delay metrics between geographic regional and international endpoints to establish tunneling routing detours.

> **📚 Interactive Methodology:** The application bundles an interactive translation of the complete theoretical documentation on VPN/Proxy detection rules, available at any time.

## 🛠 Technology Stack

* **Language:** Kotlin 
* **User Interface:** Jetpack Compose, Material Design 3 (Fully supporting Light & Dark themes)
* **Networking/Moshi Serialization:** Retrofit 2, Moshi Converters with full fail-safes
* **Concurrency:** Kotlin Coroutines & Asynchronous Flows
* **Storage:** SharedPreferences for theme settings and scan options

---

## 🚀 Building and Running

You can easily build the project using standard Gradle utilities or within Android Studio:

```bash
# 1. Clone the repository
git clone https://github.com/Kasumicic/vpn-inspector.git
cd vpn-inspector

# 2. Build the Debug APK package
gradle assembleDebug
```
The compiled APK file will be written to `app/build/outputs/apk/debug/app-debug.apk`.

---

<div align="center">
  <img src="https://github.com/Kasumicic.png" width="60" style="border-radius:50%" />
  <br>
  Built with ❤️ by <b><a href="https://github.com/Kasumicic">Kasumicic</a></b>
</div>
