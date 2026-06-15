package com.kasumic.vpndetector.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasumic.vpndetector.AppConfig
import com.kasumic.vpndetector.LocaleHelper
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.RegionSelectionSubScreen
import com.kasumic.vpndetector.ui.theme.AppColors
import com.kasumic.vpndetector.ui.theme.LocalAppColors
import com.kasumic.vpndetector.utils.getCountryName
import com.kasumic.vpndetector.utils.getFlagEmoji
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    useDynamicColor: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onPrefsChanged: () -> Unit
) {
    val colors = LocalAppColors.current
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    if (currentSubScreen == "checks") {
        DiagnosticsSettingsSubScreen(
            colors = colors,
            onBack = { currentSubScreen = null }
        )
    } else if (currentSubScreen == "region") {
        val context = LocalContext.current
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val systemLanguage = Locale.getDefault().language
        val defaultLang = if (systemLanguage == "ru") "ru" else "en"
        val currentLang = sharedPrefs.getString("app_lang", defaultLang) ?: defaultLang
        val defaultRegion = if (systemLanguage == "ru") "RU" else "US"
        val currentRegion = sharedPrefs.getString("target_region", defaultRegion) ?: defaultRegion

        RegionSelectionSubScreen(
            currentLang = currentLang,
            currentRegion = currentRegion,
            onBack = { currentSubScreen = null },
            onRegionSelected = { newRegion ->
                sharedPrefs.edit().putString("target_region", newRegion).apply()
                currentSubScreen = null
                onPrefsChanged()
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = colors.primaryAction, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.tab_settings), fontSize = 24.sp, color = colors.primaryText, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_section_general),
                color = colors.secondaryActionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val context = LocalContext.current
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val systemLanguage = Locale.getDefault().language
            val defaultLang = if (systemLanguage == "ru") "ru" else "en"
            val currentLang = sharedPrefs.getString("app_lang", defaultLang) ?: defaultLang
            val defaultRegion = if (systemLanguage == "ru") "RU" else "US"
            val currentRegion = sharedPrefs.getString("target_region", defaultRegion) ?: defaultRegion

            var showLanguageDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark Theme Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dark_theme), color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onThemeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.primaryActionText,
                                checkedTrackColor = colors.primaryAction,
                                uncheckedThumbColor = colors.secondaryText,
                                uncheckedTrackColor = colors.border
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Colors Row
                    val isDynamicColorSupported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = stringResource(R.string.settings_dynamic_colors),
                                color = if (isDynamicColorSupported) colors.primaryText else colors.primaryText.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isDynamicColorSupported) stringResource(R.string.settings_dynamic_colors_desc) else stringResource(R.string.settings_dynamic_colors_unsupported),
                                color = colors.secondaryText,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = useDynamicColor && isDynamicColorSupported,
                            onCheckedChange = { newVal ->
                                if (isDynamicColorSupported) {
                                    onDynamicColorChange(newVal)
                                }
                            },
                            enabled = isDynamicColorSupported,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.primaryActionText,
                                checkedTrackColor = colors.primaryAction,
                                uncheckedThumbColor = colors.secondaryText,
                                uncheckedTrackColor = colors.border,
                                disabledCheckedThumbColor = colors.primaryActionText.copy(alpha = 0.5f),
                                disabledCheckedTrackColor = colors.primaryAction.copy(alpha = 0.5f),
                                disabledUncheckedThumbColor = colors.secondaryText.copy(alpha = 0.5f),
                                disabledUncheckedTrackColor = colors.border.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Language selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_language), color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = if (currentLang == "ru") "Русский" else "English",
                                color = colors.secondaryText,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.primaryAction
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Region selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSubScreen = "region" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_target_region), color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            val currentRegionName = remember(currentRegion, currentLang) {
                                val flag = getFlagEmoji(currentRegion)
                                val name = getCountryName(currentRegion, currentLang == "ru")
                                "$flag $name ($currentRegion)"
                            }
                            Text(
                                text = currentRegionName,
                                color = colors.secondaryText,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.primaryAction
                        )
                    }
                }
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    containerColor = colors.surface,
                    title = { Text(stringResource(R.string.settings_language), color = colors.primaryText) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ru" to "Русский", "en" to "English").forEach { (code, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            sharedPrefs.edit().putString("app_lang", code).apply()
                                            LocaleHelper.updateResourcesLegacy(context, code)
                                            showLanguageDialog = false
                                            onPrefsChanged()
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, color = colors.primaryText)
                                    if (currentLang == code) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(stringResource(R.string.dismiss_dialog), color = colors.primaryAction)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Diagnostic checks Navigation Link
            Text(
                text = stringResource(R.string.settings_section_checks),
                color = colors.secondaryActionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .clickable { currentSubScreen = "checks" },
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_section_checks),
                            color = colors.primaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_section_checks_desc),
                            color = colors.secondaryText,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.primaryAction
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsSettingsSubScreen(
    colors: AppColors,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var resetTrigger by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.settings_back),
                    tint = colors.primaryAction
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_section_checks),
                fontSize = 20.sp,
                color = colors.primaryText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.settings_section_checks_desc),
            color = colors.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(20.dp))

        val checksList = AppConfig.ALL_CHECKS

        key(resetTrigger) {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    checksList.forEachIndexed { index, module ->
                        var isChecked by remember { mutableStateOf(sharedPrefs.getBoolean(module.key, module.defaultValue)) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newVal = !isChecked
                                    sharedPrefs.edit().putBoolean(module.key, newVal).apply()
                                    isChecked = newVal
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(module.titleRes),
                                color = colors.primaryText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { newVal ->
                                    sharedPrefs.edit().putBoolean(module.key, newVal).apply()
                                    isChecked = newVal
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.primaryActionText,
                                    checkedTrackColor = colors.primaryAction,
                                    uncheckedThumbColor = colors.secondaryText,
                                    uncheckedTrackColor = colors.border
                                )
                            )
                        }

                        if (index < checksList.size - 1) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.3f)))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Settings Button
        OutlinedButton(
            onClick = {
                val editor = sharedPrefs.edit()
                checksList.forEach { module ->
                    editor.putBoolean(module.key, module.defaultValue)
                }
                editor.apply()
                resetTrigger++
            },
           modifier = Modifier.fillMaxWidth().height(48.dp),
           shape = RoundedCornerShape(12.dp),
           colors = ButtonDefaults.outlinedButtonColors(
               contentColor = colors.primaryAction
           ),
           border = BorderStroke(1.dp, colors.primaryAction.copy(alpha = 0.5f))
       ) {
           Row(
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Center
           ) {
               Icon(
                   imageVector = Icons.Default.Refresh,
                   contentDescription = null,
                   modifier = Modifier.size(18.dp)
               )
               Spacer(modifier = Modifier.width(8.dp))
               Text(
                   text = stringResource(R.string.settings_reset_checks),
                   fontSize = 15.sp,
                   fontWeight = FontWeight.Bold
               )
           }
       }
    }
}
