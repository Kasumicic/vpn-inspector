package com.kasumic.vpndetector.ui.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone

data class TimeDifference(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

fun calculateTimeDifference(): TimeDifference {
    val now = System.currentTimeMillis()
    val targetVal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val diff = targetVal - now
    if (diff <= 0) return TimeDifference(0, 0, 0, 0)
    
    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60
    return TimeDifference(days, hours, minutes, seconds)
}

@Composable
fun TimeBox(value: String, label: String) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2E32)),
        modifier = Modifier.width(46.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KeepAndroidOpenWidget() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(sharedPrefs.getBoolean("keep_android_open_dismissed", false)) }

    if (isDismissed) return

    // Ticking state
    var diffTime by remember { mutableStateOf(calculateTimeDifference()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            diffTime = calculateTimeDifference()
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.keep_android_open_dismiss_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.keep_android_open_dismiss_confirm_desc),
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sharedPrefs.edit().putBoolean("keep_android_open_dismissed", true).apply()
                        isDismissed = true
                        showConfirmDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.keep_android_open_dismiss_confirm_yes),
                        color = Color(0xFFFF4E4E),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.keep_android_open_dismiss_confirm_no),
                        color = colors.secondaryText
                    )
                }
            },
            containerColor = colors.surface,
            titleContentColor = colors.primaryText,
            textContentColor = colors.secondaryText
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFFF4E4E).copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F22)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.keep_android_open_title).uppercase(),
                    color = Color(0xFFFF4E4E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.keep_android_open_headline),
                    color = Color(0xFFFF6B6B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC92A2A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${diffTime.days}",
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.keep_android_open_days_remaining).uppercase(),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeBox(value = String.format("%02d", diffTime.hours), label = "h")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                    TimeBox(value = String.format("%02d", diffTime.minutes), label = "m")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                    TimeBox(value = String.format("%02d", diffTime.seconds), label = "s")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.keep_android_open_desc),
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { uriHandler.openUri("https://keepandroidopen.org") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC92A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.keep_android_open_visit_website),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
