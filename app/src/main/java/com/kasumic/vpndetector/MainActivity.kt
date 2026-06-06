package com.kasumic.vpndetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasumic.vpndetector.scanner.ScanResult
import com.kasumic.vpndetector.ui.theme.MyApplicationTheme
import android.content.Context
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack

data class AppColors(
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val primaryAction: Color,
    val primaryActionText: Color,
    val secondaryActionText: Color,
    val border: Color,
    val success: Color = Color(0xFF4ADE80),
    val error: Color = Color(0xFFF87171),
    val warning: Color = Color(0xFFFBBF24),
    val navSelectedIcon: Color,
    val navSelectedText: Color,
    val navIndicator: Color,
    val navUnselected: Color
)

val DarkAppColors = AppColors(
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    primaryText = Color(0xFFE6E1E5),
    secondaryText = Color(0xFFCAC4D0),
    primaryAction = Color(0xFFD0BCFF),
    primaryActionText = Color(0xFF381E72),
    secondaryActionText = Color(0xFFD0BCFF),
    border = Color(0xFF4A4458),
    navSelectedIcon = Color(0xFF381E72),
    navSelectedText = Color(0xFFE6E1E5),
    navIndicator = Color(0xFFE8DEF8),
    navUnselected = Color(0xFFCAC4D0)
)

val LightAppColors = AppColors(
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFF3EDF7),
    primaryText = Color(0xFF1D1B20),
    secondaryText = Color(0xFF49454F),
    primaryAction = Color(0xFF6750A4),
    primaryActionText = Color(0xFFFFFFFF),
    secondaryActionText = Color(0xFF6750A4),
    border = Color(0xFFE7E0EC),
    success = Color(0xFF16A34A),
    error = Color(0xFFDC2626),
    warning = Color(0xFFD97706),
    navSelectedIcon = Color(0xFFFFFFFF),
    navSelectedText = Color(0xFF1D1B20),
    navIndicator = Color(0xFF6750A4),
    navUnselected = Color(0xFF49454F)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = sharedPrefs.getString("app_lang", null)
        if (lang != null) {
            super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = sharedPrefs.getString("app_lang", "ru") ?: "ru"
        LocaleHelper.updateResourcesLegacy(this, lang)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(sharedPrefs.getBoolean("is_dark_theme", true)) }
            var hasCompletedOnboarding by remember {
                mutableStateOf(sharedPrefs.contains("app_lang") && sharedPrefs.contains("target_region"))
            }
            val colors = if (isDarkTheme) DarkAppColors else LightAppColors

            CompositionLocalProvider(LocalAppColors provides colors) {
                if (!hasCompletedOnboarding) {
                    MyApplicationTheme(darkTheme = isDarkTheme) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = colors.background
                        ) {
                            OnboardingScreen(
                                isDarkTheme = isDarkTheme,
                                onCompleted = { selectedLang, selectedRegion ->
                                    sharedPrefs.edit()
                                        .putString("app_lang", selectedLang)
                                        .putString("target_region", selectedRegion)
                                        .apply()
                                    LocaleHelper.updateResourcesLegacy(this@MainActivity, selectedLang)
                                    hasCompletedOnboarding = true
                                    recreate()
                                }
                            )
                        }
                    }
                } else {
                    MyApplicationTheme(darkTheme = isDarkTheme) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = colors.background
                        ) {
                            MainScreen(
                                isDarkTheme = isDarkTheme,
                                onThemeChange = { newTheme ->
                                    isDarkTheme = newTheme
                                    sharedPrefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                                },
                                onPrefsChanged = {
                                    recreate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onPrefsChanged: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableStateOf(sharedPrefs.getInt("selected_tab", 0)) }
    val colors = LocalAppColors.current

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                contentColor = colors.secondaryText,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.tab_scan)) },
                    label = { Text(stringResource(R.string.tab_scan)) },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0 
                        sharedPrefs.edit().putInt("selected_tab", 0).apply()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.navSelectedIcon,
                        selectedTextColor = colors.navSelectedText,
                        indicatorColor = colors.navIndicator,
                        unselectedIconColor = colors.navUnselected,
                        unselectedTextColor = colors.navUnselected
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.tab_settings)) },
                    label = { Text(stringResource(R.string.tab_settings)) },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1 
                        sharedPrefs.edit().putInt("selected_tab", 1).apply()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.navSelectedIcon,
                        selectedTextColor = colors.navSelectedText,
                        indicatorColor = colors.navIndicator,
                        unselectedIconColor = colors.navUnselected,
                        unselectedTextColor = colors.navUnselected
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.tab_info)) },
                    label = { Text(stringResource(R.string.tab_info)) },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2 
                        sharedPrefs.edit().putInt("selected_tab", 2).apply()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.navSelectedIcon,
                        selectedTextColor = colors.navSelectedText,
                        indicatorColor = colors.navIndicator,
                        unselectedIconColor = colors.navUnselected,
                        unselectedTextColor = colors.navUnselected
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> VpnScannerApp()
                1 -> SettingsScreen(isDarkTheme, onThemeChange, onPrefsChanged)
                2 -> AboutScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onPrefsChanged: () -> Unit) {
    val colors = LocalAppColors.current
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    if (currentSubScreen == "checks") {
        DiagnosticsSettingsSubScreen(
            colors = colors,
            onBack = { currentSubScreen = null }
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

            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentLang = sharedPrefs.getString("app_lang", "ru") ?: "ru"
            val currentRegion = sharedPrefs.getString("target_region", "RU") ?: "RU"

            var showLanguageDialog by remember { mutableStateOf(false) }
            var showRegionDialog by remember { mutableStateOf(false) }

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
                            .clickable { showRegionDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_target_region), color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = when (currentRegion) {
                                    "RU" -> "🇷🇺 Россия (RU)"
                                    "US" -> "🇺🇸 United States (US)"
                                    "DE" -> "🇩🇪 Germany (DE)"
                                    "KZ" -> "🇰🇿 Казахстан (KZ)"
                                    "UA" -> "🇺🇦 Украина (UA)"
                                    else -> "🏳️ $currentRegion"
                                },
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

            if (showRegionDialog) {
                var customCodeInput by remember { mutableStateOf(if (currentRegion !in listOf("RU", "US", "DE", "KZ", "UA")) currentRegion else "") }
                var isCustomClicked by remember { mutableStateOf(currentRegion !in listOf("RU", "US", "DE", "KZ", "UA")) }
                var isValError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showRegionDialog = false },
                    containerColor = colors.surface,
                    title = { Text(stringResource(R.string.settings_target_region), color = colors.primaryText) },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(
                                "RU" to "🇷🇺 Россия (RU)",
                                "US" to "🇺🇸 United States (US)",
                                "DE" to "🇩🇪 Germany (DE)",
                                "KZ" to "🇰🇿 Казахстан (KZ)",
                                "UA" to "🇺🇦 Украина (UA)"
                            )

                            presets.forEach { (code, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            sharedPrefs.edit().putString("target_region", code).apply()
                                            showRegionDialog = false
                                            onPrefsChanged()
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, color = colors.primaryText)
                                    if (!isCustomClicked && currentRegion == code) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                                    }
                                }
                            }

                            // Custom option
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isCustomClicked = true }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.custom_region_label), color = colors.primaryText)
                                    if (isCustomClicked) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                                    }
                                }

                                if (isCustomClicked) {
                                    OutlinedTextField(
                                        value = customCodeInput,
                                        onValueChange = {
                                            if (it.length <= 2) {
                                                customCodeInput = it.uppercase().filter { ch -> ch.isLetter() }
                                                isValError = false
                                            }
                                        },
                                        placeholder = { Text("US") },
                                        singleLine = true,
                                        isError = isValError,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primaryAction,
                                            unfocusedBorderColor = colors.border,
                                            focusedLabelColor = colors.primaryAction,
                                            unfocusedLabelColor = colors.secondaryText
                                        )
                                    )
                                    if (isValError) {
                                        Text(
                                            text = stringResource(R.string.custom_region_error),
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showRegionDialog = false }) {
                                Text(stringResource(R.string.dismiss_dialog), color = colors.primaryAction)
                            }
                            if (isCustomClicked) {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    if (customCodeInput.length != 2) {
                                        isValError = true
                                    } else {
                                        sharedPrefs.edit().putString("target_region", customCodeInput).apply()
                                        showRegionDialog = false
                                        onPrefsChanged()
                                    }
                                }) {
                                    Text(stringResource(R.string.btn_save_and_continue), color = colors.primaryAction, fontWeight = FontWeight.Bold)
                                }
                            }
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
    val context = androidx.compose.ui.platform.LocalContext.current
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

        val checksList = listOf(
            "check_geo" to R.string.geo_analysis_title,
            "check_ipv6_leak" to R.string.ipv6_leak_title,
            "check_direct_api" to R.string.direct_vpn_api_title,
            "check_system_proxy" to R.string.proxy_settings_title,
            "check_vpn_apps" to R.string.app_packages_title,
            "check_not_vpn_capability" to R.string.not_vpn_title,
            "check_interfaces" to R.string.interfaces_title,
            "check_mtu_anomalies" to R.string.mtu_anomalies_title,
            "check_local_proxies" to R.string.local_proxy_title,
            "check_fake_ip" to R.string.fake_ip_title,
            "check_dns_servers" to R.string.dns_check_title,
            "check_latency" to R.string.snitch_title
        )

        key(resetTrigger) {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    checksList.forEachIndexed { index, (key, titleRes) ->
                        var isChecked by remember { mutableStateOf(sharedPrefs.getBoolean(key, true)) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newVal = !isChecked
                                    sharedPrefs.edit().putBoolean(key, newVal).apply()
                                    isChecked = newVal
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(titleRes),
                                color = colors.primaryText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { newVal ->
                                    sharedPrefs.edit().putBoolean(key, newVal).apply()
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
                checksList.forEach { (key, _) ->
                    editor.putBoolean(key, true)
                }
                editor.apply()
                resetTrigger++
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primaryAction
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryAction.copy(alpha = 0.5f))
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

data class TimeDifference(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

fun calculateTimeDifference(): TimeDifference {
    val now = System.currentTimeMillis()
    val targetVal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        set(2026, java.util.Calendar.SEPTEMBER, 1, 0, 0, 0)
        set(java.util.Calendar.MILLISECOND, 0)
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = LocalAppColors.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(sharedPrefs.getBoolean("keep_android_open_dismissed", false)) }

    if (isDismissed) return

    // Ticking state
    var diffTime by remember { mutableStateOf(calculateTimeDifference()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
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
            // Dismiss Button (Top End)
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
        Text(stringResource(R.string.version_title), textAlign = TextAlign.Center, color = colors.secondaryText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.source_code),
            color = colors.primaryAction,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { uriHandler.openUri("https://github.com/Kasumicic/vpn-inspector") },
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
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
            MethodologyBullet(stringResource(R.string.bullet_fake_ip_title), stringResource(R.string.bullet_fake_ip_desc), colors)
            MethodologyBullet(stringResource(R.string.bullet_latency_title), stringResource(R.string.bullet_latency_desc), colors)
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

        Text(stringResource(R.string.developer_title), color = colors.secondaryActionText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { uriHandler.openUri("https://github.com/Kasumicic") }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = "https://github.com/Kasumicic.png",
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
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showMethodology = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
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
            text = "VPN INSPECTOR",
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
                        text = "Trust Score: ${uiState.trustScore}%",
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

fun formatScanSummary(context: android.content.Context, uiState: ScannerUiState): String {
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
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

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
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(formatScanSummary(context, uiState)))
                    android.widget.Toast.makeText(context, context.getString(R.string.toast_result_copied), android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(56.dp).background(colors.surface, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_content_description), tint = colors.primaryAction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    isDarkTheme: Boolean,
    onCompleted: (lang: String, region: String) -> Unit
) {
    val colors = LocalAppColors.current
    var selectedLanguage by remember { mutableStateOf("ru") }
    var selectedRegion by remember { mutableStateOf("RU") }
    var customRegionCode by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val presetRegions = listOf(
        "RU" to ("🇷🇺 " + (if (selectedLanguage == "ru") "Россия" else "Russia")),
        "US" to ("🇺🇸 " + (if (selectedLanguage == "ru") "США" else "USA")),
        "DE" to ("🇩🇪 " + (if (selectedLanguage == "ru") "Германия" else "Germany")),
        "KZ" to ("🇰🇿 " + (if (selectedLanguage == "ru") "Казахстан" else "Kazakhstan")),
        "UA" to ("🇺🇦 " + (if (selectedLanguage == "ru") "Украина" else "Ukraine"))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colors.primaryAction,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (selectedLanguage == "ru") "Добро пожаловать в VPN Inspector" else "Welcome to VPN Inspector",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (selectedLanguage == "ru") 
                "Для продолжения выберите язык приложения и целевой регион для проверки GeoIP (страна, в которой вы ожидаете находиться без активного VPN)." 
                else "Please select your preferred app language and target GeoIP region to start. The target region is the country you expect to be geolocated in when no VPN is active.",
            fontSize = 14.sp,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Language block
        Text(
            text = if (selectedLanguage == "ru") "Язык приложения / Language" else "Application Language / Язык",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("ru" to "Русский", "en" to "English").forEach { (code, name) ->
                val isSelected = selectedLanguage == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primaryAction else colors.surface)
                        .border(1.dp, if (isSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                        .clickable { selectedLanguage = code }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) colors.primaryActionText else colors.primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Target Region block
        Text(
            text = if (selectedLanguage == "ru") "Целевой регион GeoIP / Target Region" else "Target GeoIP Region / Целевой регион",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetRegions.forEach { (code, name) ->
                val isSelected = !isCustomSelected && selectedRegion == code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primaryAction.copy(alpha = 0.15f) else colors.surface)
                        .border(1.dp, if (isSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                        .clickable {
                            isCustomSelected = false
                            selectedRegion = code
                            showError = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = name, color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            isCustomSelected = false
                            selectedRegion = code
                            showError = false
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAction, unselectedColor = colors.secondaryText)
                    )
                }
            }

            // Custom Region Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCustomSelected) colors.primaryAction.copy(alpha = 0.15f) else colors.surface)
                    .border(1.dp, if (isCustomSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                    .clickable {
                        isCustomSelected = true
                        showError = false
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedLanguage == "ru") "Другой (свой код)" else "Other (Custom code)",
                    color = colors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                RadioButton(
                    selected = isCustomSelected,
                    onClick = {
                        isCustomSelected = true
                        showError = false
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAction, unselectedColor = colors.secondaryText)
                )
            }
        }

        if (isCustomSelected) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customRegionCode,
                onValueChange = { input ->
                    if (input.length <= 2) {
                        customRegionCode = input.uppercase().filter { it.isLetter() }
                        showError = false
                    }
                },
                label = { Text(if (selectedLanguage == "ru") "Код страны (2 буквы, например US)" else "Country code (2 letters, e.g. US)") },
                placeholder = { Text("US") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = showError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryAction,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primaryAction,
                    unfocusedLabelColor = colors.secondaryText
                )
            )
            if (showError) {
                Text(
                    text = if (selectedLanguage == "ru") "Код страны должен состоять ровно из 2 букв" else "Country code must be exactly 2 letters",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val region = if (isCustomSelected) {
                    if (customRegionCode.length != 2) {
                        showError = true
                        return@Button
                    }
                    customRegionCode
                } else {
                    selectedRegion
                }
                onCompleted(selectedLanguage, region)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAction,
                contentColor = colors.primaryActionText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (selectedLanguage == "ru") "Сохранить и продолжить" else "Save and Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
