package com.kasumic.vpndetector.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kasumic.vpndetector.AppConfig
import com.kasumic.vpndetector.MethodologyViewerScreen
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.ui.components.KeepAndroidOpenWidget
import com.kasumic.vpndetector.ui.theme.AppColors
import com.kasumic.vpndetector.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val colors = LocalAppColors.current
    var showMethodology by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryAction, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.app_name), fontSize = 24.sp, color = colors.primaryText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.version_title, AppConfig.VERSION_NAME), textAlign = TextAlign.Center, color = colors.secondaryText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.source_code),
            color = colors.primaryAction,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { uriHandler.openUri("https://github.com/Kasumicic/vpn-inspector") },
            textDecoration = TextDecoration.Underline
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(stringResource(R.string.about_app_title), color = colors.secondaryActionText, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.about_app_desc),
            color = colors.secondaryText, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(stringResource(R.string.key_points_title), color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            MethodologyBullet(stringResource(R.string.bullet_geoip_title), stringResource(R.string.bullet_geoip_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_ipv6_title), stringResource(R.string.bullet_ipv6_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_system_vpn_title), stringResource(R.string.bullet_system_vpn_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_system_proxy_title), stringResource(R.string.bullet_system_proxy_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_direct_packages_title), stringResource(R.string.bullet_direct_packages_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_not_vpn_title), stringResource(R.string.bullet_not_vpn_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_interfaces_title), stringResource(R.string.bullet_interfaces_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_mtu_title), stringResource(R.string.bullet_mtu_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_local_proxy_title), stringResource(R.string.bullet_local_proxy_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_latency_title), stringResource(R.string.bullet_latency_desc), colors)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.provider_info_title),
                    color = colors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.provider_info_desc),
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showMethodology = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAction,
                contentColor = colors.primaryActionText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_read_methodology), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(modifier = Modifier.height(24.dp))

        Text(stringResource(R.string.developer_title), color = colors.primaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.github_avatar),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.primaryAction, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Kasumicic", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("github.com/Kasumicic", color = colors.primaryAction, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { uriHandler.openUri("https://github.com/Kasumicic/vpn-inspector") }
                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAction.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star Project",
                        tint = colors.primaryAction,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.support_star_title),
                        color = colors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.support_star_desc),
                        color = colors.secondaryText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        KeepAndroidOpenWidget()
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showMethodology) {
        Dialog(
            onDismissRequest = { showMethodology = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            MethodologyViewerScreen(
                onDismiss = { showMethodology = false },
                colors = colors
            )
        }
    }
}

@Composable
fun MethodologyBullet(title: String, desc: String, colors: AppColors) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(title, color = colors.primaryAction, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
        Text(desc, color = colors.secondaryText, fontSize = 14.sp)
    }
}
