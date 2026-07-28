package com.kasumic.vpndetector.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.UriHandler
import java.util.Locale

fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🏳️"
    val firstChar = countryCode[0].uppercaseChar() - 'A' + 0x1F1E6
    val secondChar = countryCode[1].uppercaseChar() - 'A' + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

fun getCountryName(countryCode: String, isRussian: Boolean): String {
    val localeOfCountry = Locale("", countryCode)
    val displayLocale = if (isRussian) Locale("ru") else Locale("en")
    val name = localeOfCountry.getDisplayCountry(displayLocale)
    return if (name.isNotEmpty() && name != countryCode) name else countryCode
}

class SafeUriHandler(private val context: Context) : UriHandler {
    override fun openUri(uri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val msg = context.getString(com.kasumic.vpndetector.R.string.browser_not_found)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // catch any other issues
        }
    }
}
