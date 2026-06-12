package com.kasumic.vpndetector

import androidx.annotation.StringRes

data class CheckModule(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val stepRes: Int,
    val defaultValue: Boolean = true
)

object AppConfig {
    const val VERSION_NAME = "1.6.2"
    const val VERSION_CODE = 9

    val ALL_CHECKS = listOf(
        CheckModule(
            key = "check_geo",
            titleRes = R.string.geo_analysis_title,
            stepRes = R.string.scan_step_1
        ),
        CheckModule(
            key = "check_datacenter",
            titleRes = R.string.datacenter_check_title,
            stepRes = R.string.scan_step_13
        ),
        CheckModule(
            key = "check_ipv6_leak",
            titleRes = R.string.ipv6_leak_title,
            stepRes = R.string.scan_step_2
        ),
        CheckModule(
            key = "check_direct_api",
            titleRes = R.string.direct_vpn_api_title,
            stepRes = R.string.scan_step_3
        ),
        CheckModule(
            key = "check_system_proxy",
            titleRes = R.string.proxy_settings_title,
            stepRes = R.string.scan_step_4
        ),
        CheckModule(
            key = "check_vpn_apps",
            titleRes = R.string.app_packages_title,
            stepRes = R.string.scan_step_5
        ),
        CheckModule(
            key = "check_not_vpn_capability",
            titleRes = R.string.not_vpn_title,
            stepRes = R.string.scan_step_6
        ),
        CheckModule(
            key = "check_interfaces",
            titleRes = R.string.interfaces_title,
            stepRes = R.string.scan_step_7
        ),
        CheckModule(
            key = "check_mtu_anomalies",
            titleRes = R.string.mtu_anomalies_title,
            stepRes = R.string.scan_step_8
        ),
        CheckModule(
            key = "check_local_proxies",
            titleRes = R.string.local_proxy_title,
            stepRes = R.string.scan_step_9
        ),
        CheckModule(
            key = "check_dns_servers",
            titleRes = R.string.dns_check_title,
            stepRes = R.string.scan_step_11
        ),
        CheckModule(
            key = "check_latency",
            titleRes = R.string.snitch_title,
            stepRes = R.string.scan_step_12
        )
    )
}
