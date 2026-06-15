package com.kasumic.vpndetector.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kasumic.vpndetector.R
import com.kasumic.vpndetector.ui.theme.LocalAppColors

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    useDynamicColor: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onPrefsChanged: () -> Unit
) {
    val context = LocalContext.current
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
                1 -> SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    useDynamicColor = useDynamicColor,
                    onThemeChange = onThemeChange,
                    onDynamicColorChange = onDynamicColorChange,
                    onPrefsChanged = onPrefsChanged
                )
                2 -> AboutScreen()
            }
        }
    }
}
