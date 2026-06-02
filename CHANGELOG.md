# Changelog

All notable changes to this project will be documented in this file.

## [1.5.0] - Real IPv6 Leak Detection Added

### 🔬 What's New:
- **Real IPv6 Leak Detector** (Step 11/11): Implemented intelligent checking of VPN routing vulnerabilities. The app initiates an active check to request an external IPv6 address via a dedicated, dual-stack or IPv6-only host and correlates its geolocation country with the IPv4 geolocation country.
- **Intelligent Split-tunnel / Leak Analysis**: Compares IPv4 and IPv6 country locations. If they differ (for example, IPv4 points to US but IPv6 points to RU or another origin country), the app immediately signals an active real IP leak and provides remediation advice.
- **Fail-safe Geolocation Resolve**: Integrated a multi-tiered fallback mechanism for IPv6 geolocation resolution (using Sypex Geo, ipapi.co, ipwho.is, and freeipapi.com) to guarantee high accuracy.
- **Methodology Documentation Update**: Updated the interactive methodology section and in-app documentation to outline testing goals, mechanics, and mitigation of IPv6 leaks.

## [1.4.0] - RKN Methodology Compliance & Mobile Performance Optimization

### 🔬 What's New:
- **Sypex Geo Migration**: Switched primary GeoIP resolver to `api.sypexgeo.net` and optimized request timeouts (15 seconds limit) to eliminate hangs on low-quality mobile connections.
- **Connection Spoofing**: Configured a legitimate Android Chrome User-Agent header for API requests to emulate browse traffic.
- **IPv6 Resolve Priority**: Implemented a custom DNS resolver `IPv6FirstDns` prioritizing IPv6 queries over carrier mobile networks where IPv6 is present.
- **Fail-safe IP Extraction**: Added automatic fallback to `api64.ipify.org` for robust public IP address retrieval during primary service outages.
- **Step-by-Step Progress Tracking**: Introduced real-time scan progress feedback directly on the Home screen dashboard (e.g. "Scanning local interfaces (7/11)...").
- **Extended Detection Checks (RKN/Methodology compliance)**:
  - **System-level Proxies**: Analyzes JVM properties like `System.getProperty("http.proxyHost")` and network parameters.
  - **Capabilities `NOT_VPN` check**: Inspects whether the active connection capability flags contain `NET_CAPABILITY_NOT_VPN`.
- **Expanded Known VPN Target Database**:
  - Significantly expanded direct target scanning package identifiers to detect installed apps like *ByeDPI*, *AmneziaVPN*, *TunnelBear*, *Windscribe*, *Tor Browser*, *Orbot (Tor)*, *AntiZabor*, *Zaborona VPN*, etc.
  - Added queries block updates and `QUERY_ALL_PACKAGES` permission in `AndroidManifest.xml` to regain visibility on Android 11+.
- **GitHub Reference Link**: Added interactive GitHub repository link in both the "About" and "Methodology" sections.

### ⚙️ Fixes & Reliability:
- **Network Error State Treatment**: Network timeouts or failures on GeoIP resolve now correctly yield an orange `WARNING` / `ERROR` indicator instead of mistakenly displaying a green `SUCCESS` check.
- **Target SDK Compatibility**: Implemented Class-reflection wrapper fallback on legacy Android environments (< API 29) to safely retrieve `VpnTransportInfo` details.

## [1.3.0] - Latency Analysis (SNITCH) & Saved State Management

### 🔬 What's New:
- **SNITCH Latency Analysis**: Developed a system-level RTT (Round-Trip Time) analysis module. Measures physical route delays to regional and international locations to detect tunneling anomalies and proxy intercepts.
- **About App Information**: Expanded descriptive breakdowns on active scans to cover Latency analysis under the Methodology definitions.

### ⚙️ Fixes:
- **State Persistence**: Resolved an issue where user theme selections (Light vs. Dark mode) reset on application launch. Theme states are now fully persisted in private Shared Preferences.

## [1.2.0] - Release 1.2.0 — Secure API Provider, Custom Adaptive Icon & Production Signing

### ⚠️ IMPORTANT NOTICE: Previous Debug Build Conflict
We have migrated to release signing configuration (`release.jks`). Since the previous development build was signed with standard Android debug credentials, this update **will conflict** during installation.

**Actions Required:**
1. Uninstall any older iterations of the *VPN Inspector* app from your device.
2. Install the new APK file download.
> *Any future updates (v1.3.0 and up) will install automatically without requiring uninstallation.*

### 🔬 What's New:
- **Secure GeoIP (ipwho.is Migration)**: Re-routed GeoIP queries through secure `HTTPS` protocol endpoints to preserve communication privacy.
- **Custom Adaptive Launcher Icon**: Replaced default launcher assets with custom high-contrast adaptive icons fitting system environments across launchers.

### ⚙️ Fixes:
- **Moshi Parsing Reliability**: Configured reflective `KotlinJsonAdapterFactory` globally on Moshi network bindings to handle dynamic API layouts without crashing.
- **Java 11 Compliant Target**: Upgraded configuration parameters aligning compilation and target levels (SDK 34 / Java 11) for build stability.
- **Resource Visibility**: Patched asset compile issues where AAPT would occasionally fail identifying launcher structures.
