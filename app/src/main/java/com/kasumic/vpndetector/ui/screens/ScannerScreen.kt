package com.kasumic.vpndetector.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasumic.vpndetector.DecisionState
import com.kasumic.vpndetector.MainViewModel
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.ScannerUiState
import com.kasumic.vpndetector.scanner.ScanResult
import com.kasumic.vpndetector.ui.theme.LocalAppColors

@Composable
fun VpnScannerApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedResult by remember { mutableStateOf<ScanResult?>(null) }
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(ipAddress = uiState.ipAddress)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StatusCard(uiState = uiState)

        Spacer(modifier = Modifier.height(16.dp))

        ResultsList(
            results = uiState.results,
            modifier = Modifier.weight(1f),
            onResultClick = { selectedResult = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ScanAndActions(
            uiState = uiState,
            onScanClick = { viewModel.startScan() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.api_notice_text),
            color = colors.secondaryText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    if (selectedResult != null) {
        ResultDetailsDialog(
            result = selectedResult!!,
            onDismiss = { selectedResult = null }
        )
    }
}

@Composable
fun ResultDetailsDialog(result: ScanResult, onDismiss: () -> Unit) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(text = result.moduleName, color = colors.primaryText, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(stringResource(R.string.test_tag_desc_label), fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                Text(result.description, color = colors.secondaryText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(stringResource(R.string.test_tag_readings_label), fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                val detailColor = when {
                    result.isRisky -> colors.error
                    result.isError -> colors.warning
                    else -> colors.success
                }
                Text(result.details, color = detailColor, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (result.isRisky && result.fixSuggestion.isNotEmpty()) {
                    Text(stringResource(R.string.test_tag_fix_label), fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                    Text(result.fixSuggestion, color = colors.primaryText, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss_dialog), color = colors.secondaryActionText)
            }
        }
    )
}

@Composable
fun HeaderSection(ipAddress: String) {
    val colors = LocalAppColors.current
    var isBlurred by remember { mutableStateOf(true) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            color = colors.secondaryActionText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isBlurred = !isBlurred }.padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.header_current_ip),
                color = colors.secondaryText,
                fontSize = 16.sp
            )
            Text(
                text = if (isBlurred) "•".repeat(ipAddress.length.coerceAtLeast(10)) else ipAddress,
                color = colors.primaryText,
                fontSize = 16.sp,
                modifier = if (isBlurred) Modifier.blur(4.dp) else Modifier
            )
        }
    }
}

@Composable
fun StatusCard(uiState: ScannerUiState) {
    val colors = LocalAppColors.current

    val statusText = when {
        uiState.isScanning -> uiState.currentScanStatus
        !uiState.scanCompleted -> stringResource(R.string.status_system_ready)
        uiState.decision == DecisionState.DETECTED -> stringResource(R.string.status_bypass_detected)
        uiState.decision == DecisionState.NEEDS_CHECK -> stringResource(R.string.status_check_required)
        else -> stringResource(R.string.status_system_clean)
    }

    val statusColor = when {
        uiState.isScanning -> colors.primaryText
        !uiState.scanCompleted -> colors.secondaryText
        uiState.decision == DecisionState.DETECTED -> colors.error
        uiState.decision == DecisionState.NEEDS_CHECK -> colors.warning
        else -> colors.success
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isScanning) {
                CircularProgressIndicator(color = colors.primaryAction)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            if (uiState.scanCompleted && !uiState.isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.trust_score_label, uiState.trustScore.toString()),
                        color = colors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ResultsList(results: List<ScanResult>, modifier: Modifier = Modifier, onResultClick: (ScanResult) -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = results,
            key = { it.moduleName }
        ) { result ->
            ResultItem(result, onResultClick)
        }
    }
}

@Composable
fun ResultItem(result: ScanResult, onClick: (ScanResult) -> Unit) {
    val colors = LocalAppColors.current
    val iconColor = when {
        result.isRisky -> colors.error
        result.isError -> colors.warning
        else -> colors.success
    }
    val icon = when {
        result.isRisky -> Icons.Default.Warning
        result.isError -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable { onClick(result) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = result.moduleName,
                color = colors.primaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = result.details,
                color = colors.secondaryText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

fun formatScanSummary(context: Context, uiState: ScannerUiState): String {
    val sb = java.lang.StringBuilder()
    sb.append(context.getString(R.string.share_report_title))
    sb.append(context.getString(R.string.share_final_status))
    when (uiState.decision) {
        DecisionState.DETECTED -> sb.append(context.getString(R.string.share_result_detected))
        DecisionState.NEEDS_CHECK -> sb.append(context.getString(R.string.share_result_needs_check))
        else -> sb.append(context.getString(R.string.share_result_clean))
    }
    sb.append(context.getString(R.string.share_trust_score, uiState.trustScore.toString()))
    sb.append(context.getString(R.string.share_results_header))
    for (result in uiState.results) {
        val icon = when {
            result.isRisky -> "🔴"
            result.isError -> "🟡"
            else -> "🟢"
        }
        sb.append("$icon ${result.moduleName}: ${result.details}\n")
    }
    return sb.toString()
}

@Composable
fun ScanAndActions(uiState: ScannerUiState, onScanClick: () -> Unit) {
    val colors = LocalAppColors.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onScanClick,
            enabled = !uiState.isScanning,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAction,
                contentColor = colors.primaryActionText,
                disabledContainerColor = colors.border,
                disabledContentColor = colors.secondaryText
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (uiState.isScanning) stringResource(R.string.btn_analysis_in_progress) else stringResource(R.string.btn_start_scan),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (uiState.scanCompleted && !uiState.isScanning) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(formatScanSummary(context, uiState)))
                    Toast.makeText(context, context.getString(R.string.toast_result_copied), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(56.dp).background(colors.surface, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_content_description), tint = colors.primaryAction)
            }
        }
    }
}
