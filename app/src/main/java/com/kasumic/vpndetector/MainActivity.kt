package com.kasumic.vpndetector

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.kasumic.vpndetector.ui.screens.MainScreen
import com.kasumic.vpndetector.ui.theme.LocalAppColors
import com.kasumic.vpndetector.ui.theme.MyApplicationTheme
import com.kasumic.vpndetector.utils.SafeUriHandler

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = sharedPrefs.getString("app_lang", null)
        if (lang != null) {
            super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
        } else {
            val systemLanguage = java.util.Locale.getDefault().language
            val defaultLang = if (systemLanguage == "ru") "ru" else "en"
            super.attachBaseContext(LocaleHelper.setLocale(newBase, defaultLang))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val systemLanguage = java.util.Locale.getDefault().language
        val defaultLang = if (systemLanguage == "ru") "ru" else "en"
        val lang = sharedPrefs.getString("app_lang", defaultLang) ?: defaultLang
        LocaleHelper.updateResourcesLegacy(this, lang)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(sharedPrefs.getBoolean("is_dark_theme", true)) }
            var hasCompletedOnboarding by remember {
                mutableStateOf(sharedPrefs.contains("app_lang") && sharedPrefs.contains("target_region"))
            }
            val defaultDynamicColor = false
            var useDynamicColor by remember { mutableStateOf(sharedPrefs.getBoolean("use_dynamic_color", defaultDynamicColor)) }

            val safeUriHandler = remember { SafeUriHandler(this@MainActivity) }

            MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColor) {
                val appColors = LocalAppColors.current

                CompositionLocalProvider(
                    LocalUriHandler provides safeUriHandler
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = appColors.background
                    ) {
                        if (!hasCompletedOnboarding) {
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
                        } else {
                            MainScreen(
                                isDarkTheme = isDarkTheme,
                                useDynamicColor = useDynamicColor,
                                onThemeChange = { newTheme ->
                                    isDarkTheme = newTheme
                                    sharedPrefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                                },
                                onDynamicColorChange = { newDynamicColors ->
                                    useDynamicColor = newDynamicColors
                                    sharedPrefs.edit().putBoolean("use_dynamic_color", newDynamicColors).apply()
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
