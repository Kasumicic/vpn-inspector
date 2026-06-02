package com.kasumic.vpndetector

object MethodologyDataEn {
    val text = """
# 1. Document Purpose

The present methodology offers a unified approach to identifying VPN and Proxy on user devices used to bypass blocks of restricted resources.

The methodology describes the main stages of identifying VPN and Proxy, the methods used to detect them, and defines the sequence of implementing this methodology.

In addition to the methods themselves, existing limitations and challenges in their application associated with false positive detections are considered.

# 2. Terms and Abbreviations

| Term | Definition |
|---|---|
| **VPN** | Technology for creating an encrypted tunnel, where part or all of the device's traffic is routed through a remote server. |
| **Proxy** | An application-level intermediary (HTTP proxy, SOCKS proxy, etc.) through which network requests pass. |
| **GeoIP** | Determining geographic location (country, region, city) by IP address using specialized databases or external APIs. |
| **ASN** | *Autonomous System Number* — an identifier of a network in global routing (BGP). Allows identifying the owner of the IP address and the network type. |
| **Split tunneling** | A VPN operation mode where only traffic of selected applications is routed through the tunnel, while the rest of the traffic goes directly. |
| **Software** | Software / Applications |

# 3. General Description of the Methodology and Implementation Sequence

Three main areas exist, the analysis of which allows detecting VPN usage on a client device:

- Analysis of client sessions on the server (GeoIP);
- Analysis of network connections and interfaces on the client device;
- Analysis of system settings and routing tables on the client device.

Using one or more methods of detecting circumvention tools does not guarantee a 100% accurate result due to potential false positives. The reasons for such situations are discussed in Section 4.

Analysis of client sessions on the server consists of comparing the connection IP address with a reputation database. The advantages of this method include:

- **Universality.** Ability to apply the analysis regardless of the operating system (OS) of the client device;
- **Transparency for the client.** Verification is performed on the server side and does not require modifying the client application.
- **Speed.** Fast implementation compared to other methods.
- **Simplicity.** Relative simplicity of checks.

Taking these advantages into account, the implementation of client session analysis on the server should be done as the first stage of implementing VPN detection methods on client devices.

A more detailed description of the analysis of client sessions on the server is given in Section 5.

To increase the detection accuracy, client-side checks should be implemented as the second stage. Since more than half of client devices are mobile devices running Android and iOS, and 80% of applications capable of detecting circumvention tools are installed on these devices, the implementation of network connection/interface analysis and system settings/routing tables analysis should be implemented precisely in this segment. Implementing checks on devices running other OSs should be deferred to subsequent, later stages.

The implementation of checks on client devices should be divided into two sub-stages. At the first sub-stage, direct checks indicating the use of VPN and Proxy should be implemented. The second sub-stage is aimed at improving detection accuracy and reducing false positives by implementing indirect indicators of VPN and Proxy usage. Detailed descriptions of analysis methods on client devices are covered in Sections 6 and 7.

The final stage is aimed at achieving maximum coverage of client devices. A detailed description of analysis methods and existing problems for this stage is discussed in Section 8.

**Table 1. Stages of implementing the methodology for detecting circumvention tools on client devices**

| Stage | Name | Description |
|---|---|---|
| 1 | GeoIP | VPN detection on the server side by comparing IP with reputation databases |
| 2a | Mobile devices | Analysis of direct indicators of VPN usage on Android and iOS |
| 2b | Accuracy | Analysis of indirect indicators of VPN usage on Android and iOS |
| 3 | Device coverage | Analysis on other client devices |

# 4. False Positives and Ways to Minimize Them

In the process of applying the algorithm, situations may arise where indicators interpreted as VPN/Proxy use occur in legitimate device usage scenarios.

Main sources of false positives:

- **Corporate VPN.** Devices connected to a corporate network via a work VPN to access internal resources.
- **Antivirus Software and Security Tools.** Some antiviruses and firewalls create virtual interfaces or modify routing to filter traffic.
- **Docker, WSL2, Hyper-V, VirtualBox, QEMU/KVM, Android Emulator** and other virtualization and containerization environments that create virtual adapters, routes, and private subnets.
- **NAT** may lead to GeoIP databases identifying the egress point in a region different from the device's location due to routing and telecommunication specifics.

A list of measures to minimize false positives is given below:

- **Whitelisting.** Creation and maintenance of a database of known corporate VPNs and legitimate proxy servers.
- **Connection Dynamics Analysis.** Accounting for historical data. If a device uses a corporate VPN during working hours and disconnects outside of them, this scenario can be identified and excluded.
- **Port and Protocol Verification.** Additional analysis of used ports and tunneling protocols. Corporate VPNs often use standard ports (e.g., 443 for SSL VPN), whereas circumvention services may use special or dynamic ports.
- **Delayed Re-check.** If an unambiguous decision cannot be made, it is recommended to perform a secondary analysis after some time to exclude temporary network anomalies.
- **Integration with Other Data Sources.** Using additional factors: device GPS data, cell tower info, history of previous successful authentications.

# 5. Stage 1: GeoIP

## 5.1. Stage Goals

The goal of the check is to determine the device geolocation, obtained by comparing the egress IP address of the traffic stream with data from specialized reference databases (GeoIP).

## 5.2. Scope

Analysis of client sessions is conducted on the server side.

The analysis covers all devices, as well as web-based client connections.

## 5.3. GeoIP Data Sources

The system "Registry of Address and Numbering Resources of the Internet" (RANR) is intended to act as the primary reference database.

Before it is put into operation, the use of alternative databases is permitted. The most widely used ones are MaxMind and IP2Location.

To increase the accuracy of the analysis, additional commercial or internal data sources may be integrated.

## 5.4. Scenarios for Detecting Circumvention Tools

Indicators used in GeoIP analysis:

- Determining the country and region by IP address;
- Anomaly detection: frequent changes of countries, sudden changes in locations between sessions;
- Determining ASN and the organization-owner of the IP. Comparison with ranges assigned to data centers and hosting providers;
- Checking in reputation lists: VPN/Proxy addresses, TOR exit nodes, public proxies;
- Checking ranges against "whitelists" of corporate networks and CDNs to reduce false positives.

Decision algorithm:

1. Determine the public IP of the client session on the server side.
2. Determine GeoIP by this IP address.
3. Determine ASN, network type, and presence of hosting indicator.
4. Check the address against reputation lists of VPN, proxy, and TOR.
5. Correlate the obtained data with the history of past sessions, typical countries, regions, and trusted ranges.
6. Compare with client-side analysis results if available.
7. Make a decision on the use of circumvention tools based on steps 1–6:
   - According to server and client checks, the device is located in the target region, has no hosting indicator or reputation risks. Decision: **VPN not detected**.
   - Server check indicates device is abroad; client check indicates device is in the target region. Decision: **VPN detected**.
   - Server and client checks both indicate device is abroad. Decision: **Additional verification required**.
   - Server check shows hosting indicator or IP is in VPN/proxy/TOR subnets. Decision: **VPN detected** regardless of country match.

## 5.5. Alternative Device Location Sources

An alternative data source for device location is locating via mobile network base station IDs, PLMN, or Wi-Fi access point BSSIDs, followed by querying GeoIP. Coordinate estimation is done via approximation based on known infrastructure coordinates.

If the user consents to the application's use of geolocation data, this data can also be used in the analysis. GPS coordinates do not replace the GeoIP check, but only complement it.

Using coordinates increases analysis accuracy and reduces false positives.

Coordinates do not prove the absence of VPN use.

## 5.6. GeoIP Limitations

- GeoIP has limited accuracy for mobile networks, CGNAT, corporate networks, and border regions.
- Legitimate corporate VPNs often terminate in a data center and can be identified as circumvention tools by GeoIP.
- CDNs and global services can distort the device's location without any VPN usage.
- Residential proxies may have an IP of a regular utility provider and might not be detected by ASN or hosting markers.
- New VPN servers emerge faster than reputation databases can be updated.
- Geolocation changes during roaming. When a user is in roaming, the egress point of Internet access may be in the visited country, creating a discrepancy in GeoIP.

# 6. Stage 2a: Mobile Devices

## 6.1. Stage Goals

The goal of the check is to analyze direct indicators of the use of circumvention tools on mobile devices running Android and iOS.

## 6.2. Scope

Analysis of client sessions is performed on the client device.

The analysis is performed on mobile devices running Android and iOS.

## 6.3. General Principles of Analyzing Direct Indicators on Client Devices

Direct indicators of using circumvention tools are system markers of VPN and Proxy in Android and iOS. Marker collection is performed by the application on the client device via system APIs under standard user permissions.

The check should be performed at application startup, authentication, or when a key action is performed in the app (confirming a purchase/transfer, specifying a route endpoint, etc.). The key action is defined by the app developer.

Continuous monitoring or sending empty analysis results is prohibited, as it negatively affects battery life and data usage of the client device.

Client-side analysis is independent of server-side analysis and is a self-contained check, but its main purpose is to confirm and/or refine data obtained during GeoIP analysis.

## 6.4. Android

For Android, system APIs `ConnectivityManager` and `NetworkCapabilities` are used. The proposed approaches do not require `root` access and function with standard application privileges.

Direct indicators of VPN usage are:

- `IS_VPN` flag in `Score(Policies)`;
- Presence of a `VPN` transport in `Transports`;
- Presence of `VpnTransportInfo`;
- `TRANSPORT_VPN` property on the `activeNetwork` during a `hasTransport` check.

Examples of diagnostic values:

```text
Score(Policies: ):
EVER_EVALUATED&IS_UNMETERED&IS_VPN&EVER_VALIDATED&IS_VALIDATED
Transports: WIFI|VPN
VpnTransportInfo{type=1, sessionId=PCAPdroid VPN, bypassable=false
longLiveTcpConnectionsExpensive=false}
```

For Proxy detection, system properties `System.getProperty` and other available system settings are analyzed. If Proxy details (IP and port) are present in system settings, it is likely that all traffic is routed through it.

Typical Proxy port lists by technology:

- `SOCKS`: `1080, 9000, 5555, 16000-16100`;
- `http`: `80, 443, 3128, 3127, 8000, 8080, 8081, 8888`;
- Transparent Proxy: `80, 443, 4080, 7000/7044, 8082, 12345`;
- `Tor`: `9050, 9051, 9150`.

## 6.5. iOS

On iOS, access to system data is heavily restricted. Therefore, direct detection on iOS is highly difficult. Direct indicators are only visible if the application itself creates configurations that can be interpreted as circumvention tools.

To detect system Proxy settings, the system API `CFNetworkCopySystemProxySettings()` is used.

The presence of an IP and port indicates a Proxy is active and all device traffic is routed through it.

## 6.6. Mobile Device Limitations

- Residential proxies and scenarios where the public IP looks like a normal provider address.
- Privacy and filtration systems if they are not labeled as VPNs by the OS.

# 7. Stage 2b: Accuracy

## 7.1. Stage Goals

The goal of the check is to analyze indirect indicators of using circumvention tools on mobile devices running Android and iOS.

## 7.2. Scope

Analysis of client sessions is performed on the client device.

The analysis is performed on mobile devices running Android and iOS.

## 7.3. General Principles of Analyzing Indirect Indicators on Client Devices

Indirect indicators of using circumvention tools include interface names, MTU values, routing table anomalies, and modified DNS settings. Indirect indicators can also occur during normal device usage without any circumvention tools. Hence, using only indirect indicators for decision making is not permitted. They are used solely to increase the accuracy of detection results.

Collection of indirect indicators is performed within a unified data collection process.

## 7.4. Android

As an indirect indicator, the `NOT_VPN` flag in `Capabilities` can be checked. For regular networks, this flag is present; it is absent when VPN is active.

```text
Without VPN. Transports: WIFI Capabilities:
NOT_METERED&INTERNET&NOT_RESTRICTED&TRUSTED&NOT_VPN&VALIDATED&NOT_ROAMING
&FOREGROUND&NOT_CONGESTED&NOT_SUSPENDED&NOT_VCN_MANAGED

With VPN. Transports: WIFI|VPN Capabilities:
NOT_METERED&INTERNET&NOT_RESTRICTED&TRUSTED&VALIDATED&NOT_ROAMING&FOREGRO
UND&NOT_CONGESTED&NOT_SUSPENDED&NOT_VCN_MANAGED
```

Interface names like `tun0`, `tun1`, `tap0`, `wg0`, `ppp0`, `ipsec` can point to an active VPN connection and are indirect indicators, because similar interface names can be created by antiviruses, content filters, corporate security software, system components, and other services.

Another tool for collecting indirect indicators in Android 12+ is service dumps like `dumpsys vpn_management` and `dumpsys activity service`, which provide a list of active VPNs with package names.

```text
VPNs:
  0: io.github.romanvht.byedpi
     Active package name: io.github.romanvht.byedpi
     Active vpn type: 1

ServiceRecord{... io.github.romanvht.byedpi/.services.ByeDpiVpnService}
intent={act=android.net.VpnService ...}
```

## 7.5. iOS

On iOS, the following methods are used to detect indirect indicators of circumvention tools:

- Analyzing system proxy settings via `CFNetworkCopySystemProxySettings()`.
- Analyzing the `__SCOPED__` key and the list of active interfaces.
- Using `NWPathMonitor` to track network changes.
- Using `NEVPNManager` in scenarios where the app has appropriate permissions.

The `CFNetworkCopySystemProxySettings()` function returns a dictionary with current settings. The `__SCOPED__` key contains names of active interfaces. The presence of interfaces named `utun`, `tap`, `tun`, `ppp`, `ipsec` may indicate an active VPN.

An indirect indicator can also be the `P2P` flag set in the interface settings. `NWPathMonitor` allows tracking network state changes in real-time. In `pathUpdateHandler`, the `path.availableInterfaces` can be checked for interfaces with type `.other` and names containing `utun`.

We highlight `NEVPNManager` as an advanced tool for detecting indirect indicators. It provides details only on VPNs configured by the app itself or accessible via `NetworkExtension`. Using it requires special `entitlements`, which limits its generic usefulness.

The built-in `iCloud Private Relay` service should not be automatically classified as an unauthorized VPN, although its use allows bypassing blocks. Handling it requires separate logic to reduce false positives.

## 7.6. Routing

Below are routing table anomalies indicating VPN usage:

- A default route pointing to an interface other than the primary physical or wireless interface.
- Presence of specific routes directing traffic to non-standard gateways.
- Absence of a direct route to the provider gateway when connected.
- Use of non-standard MTU values.
- Routes indicating split tunneling.

These indicators can complement server-side checks or direct client checks. Detections of these indicators alone cannot be the sole basis for a "VPN detected" decision.

Limiting factors for routing-based analysis:

- Private ranges `10.0.0.0/8`, `172.16.0.0/12`, and `192.168.0.0/16` are common in office, home, and container environments.
- Route metrics change automatically based on the environment and are not unique to VPNs.
- Multiple interfaces and virtual routes are created by WSL2, Hyper-V, VirtualBox, antiviruses, parental controls, and other legitimate components.

Routing anomalies indicating Proxy usage:

- Default route set to transfer traffic to a virtual network interface.
- Specific routes present in routing tables.
- Large number of connections to a local port.
- Clients using random ports.
- Absence of direct outbound connections.

Routing analysis is not applicable to iOS as application access to other apps and network tables is restricted.

## 7.7. DNS

Analyzing DNS settings is also an indirect indicator. Explicit DNS changes to public addresses or DNS pointing inside a VPN network can be a refining indicator but is insufficient by itself.

Limiting factors for DNS-based analysis:

- DNS can be modified without a VPN (e.g., by ad blockers or filters).
- Users can set DNS manually (e.g., public or corporate DNS).
- Some VPNs do not change DNS, leaving the parent DNS active.
- Local DNS addresses and DNS routed to a virtual interface are useful only in combination with other indicators.

DNS anomalies indicating Proxy usage:

- DNS servers assigned to local addresses.
- All DNS queries directed to a virtual network interface.

## 7.8. Technical Proxy Detection Methods

- Checking for specialized tools like `proxychains` or `tsocks`.
- Identifying proxy server system processes by name or port.
- Analyzing firewall rules (`iptables` or `pf`) for local redirects.
- Monitoring active connections to non-standard or remote ports.
- Inspecting local certificates/MITM proxies.

## 7.9. Accuracy Limitations

- Under split tunneling, some apps route traffic directly, making single active network checks insufficient.
- In-app proxies, custom tunnels, and custom circumvention designs that do not reflect in system APIs.

# 8. Stage 3: Device Coverage

## 8.1. Stage Goals

The goal of the check is to analyze direct and indirect indicators of circumvention tools on any client devices.

## 8.2. Scope

Analysis of client sessions is performed on the client device.

The analysis is performed on devices running OS other than Android and iOS.

## 8.3. Device Coverage Problems

Expanding the methodology to Windows, UNIX, and MacOS is associated with difficulties. On one hand, application usage on desktop systems is minor compared to mobile devices. On the other hand, desktops use technologies that can trigger false positives.

Therefore, the decision to expand coverage should be made after collecting statistical data on mobile efficiency.

Methods for these devices should be developed after completion of the first stages.

## 8.4. MacOS

MacOS detection features:

- Calling `getifaddrs()` allows listing network interfaces, including virtual ones.
- Analyzing the routing table allows detecting non-standard routing.
- `Transparent Proxy API` is a distinct network scope useful for detection.
- System `Network Extensions` require explicit user consent.
- Enforcing routes (`enforceRoutes`) can act as a technical indicator.

## 8.5. UNIX and Linux

UNIX/Linux detection features:

- Typical interface names: `tun`, `tap`, `wg`, `utun`, `ppp`.
- Only active interfaces with state `UP` and assigned IP are used.
- VPN MTU is often smaller than standard (e.g., `1350` or `1400`).
- Multiple default routes, virtual interface routes, atypical subnets, and modified DNS as additional metrics.
- Analyzing `/etc/resolv.conf` is useful only as a supportive factor.
- Public vs. local IP comparisons and datacenter ranges as extra markers.

## 8.6. Windows

Windows detection features:

- Calling `RasEnumConnection` to get active dial-up/VPN connections.
- Calling `GetAdaptersAddresses` / `GetAdaptersInfo` to inspect network adapters.
- Interrogating virtual adapter types (`IfType = IF_TYPE_PROP_VIRTUAL`), name matches (VPN, TAP, Wintun, WireGuard, OpenVPN), and state `UP`.
- Registry checks for proxy/VPN entries.
- Routing gateway, DNS, IP discrepancies, and metric analyses.

For all desktop and UNIX platforms, the general rule applies: interfaces, routes, MTU, subnets, and DNS are indices and do not warrant hard decisions without high server-side risk.

## 8.7. Coverage Limitations

- Home routers with nested VPNs cannot be easily analyzed.
- VMs/Containers bypass native host telemetry.

# 9. Decision-Making Criteria for Detecting Circumvention Tools

A comprehensive approach is used because no single check is 100% accurate.

**Table 2. Decision Matrix**

| GeoIP | Direct indicators | Indirect indicators | Decision |
|---|---|---|---|
| NOT detected | NOT detected | NOT detected | Circumvention NOT detected |
| NOT detected | Detected | NOT detected | Circumvention NOT detected |
| NOT detected | NOT detected | Detected | Circumvention NOT detected |
| Detected | NOT detected | NOT detected | Verification required |
| NOT detected | Detected | Detected | Verification required |
| Detected | Detected | NOT detected | Circumvention detected |
| Detected | NOT detected | Detected | Circumvention detected |
| Detected | Detected | Detected | Circumvention detected |

- **Circumvention NOT detected:** all checks are negative, or indicators are found in only one of the first two checks.
- **Verification required:** indicators are found on two checks but with discrepancies. Requires a retry with expanded parameters or manual review.
- **Circumvention detected:** indicators are positive on both server and client side, or IP is confirmed as VPN/Proxy/hosting or doesn't match expected region.

# 10. Additional Detection Methods

## 10.1. Latency Analysis

`SNITCH` (*Server-side Non-intrusive Identification of Tunnelled Characteristics*) measures `RTT` (Round-Trip Time) between client and server. Route detour via VPN introduces noticeable latency. `SNITCH` matches IP geolocations with landmark timings to detect tunneling.

## 10.2. HTTP Header Analysis

Headers like `X-Forwarded-For`, `Forwarded`, or `Via` highlight transit proxy nodes. Must be used carefully as CDNs can also form these.
"""
}
