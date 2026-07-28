package com.kasumic.vpndetector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasumic.vpndetector.ui.theme.LocalAppColors
import com.kasumic.vpndetector.ui.theme.AppColors
import com.kasumic.vpndetector.utils.getCountryName
import com.kasumic.vpndetector.utils.getFlagEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    isDarkTheme: Boolean,
    onCompleted: (lang: String, region: String) -> Unit
) {
    val colors = LocalAppColors.current
    val systemLanguage = remember { java.util.Locale.getDefault().language }
    val defaultLang = remember(systemLanguage) { if (systemLanguage == "ru") "ru" else "en" }
    val defaultRegion = remember(systemLanguage) { if (systemLanguage == "ru") "RU" else "US" }
    var selectedLanguage by remember { mutableStateOf(defaultLang) }
    var selectedRegion by remember { mutableStateOf(defaultRegion) }
    var customRegionCode by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showOnboardingRegionDialog by remember { mutableStateOf(false) }

    val presetRegions = listOf(
        "RU" to ("🇷🇺 " + (if (selectedLanguage == "ru") "Россия" else "Russia")),
        "US" to ("🇺🇸 " + (if (selectedLanguage == "ru") "США" else "USA")),
        "DE" to ("🇩🇪 " + (if (selectedLanguage == "ru") "Германия" else "Germany")),
        "KZ" to ("🇰🇿 " + (if (selectedLanguage == "ru") "Казахстан" else "Kazakhstan")),
        "UA" to ("🇺🇦 " + (if (selectedLanguage == "ru") "Украина" else "Ukraine"))
    )

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        if (showOnboardingRegionDialog) {
            RegionSelectionSubScreen(
                currentLang = selectedLanguage,
                currentRegion = if (isCustomSelected) customRegionCode else selectedRegion,
                onBack = { showOnboardingRegionDialog = false },
                onRegionSelected = { newRegion ->
                    if (presetRegions.any { it.first == newRegion }) {
                        isCustomSelected = false
                        selectedRegion = newRegion
                    } else {
                        isCustomSelected = true
                        customRegionCode = newRegion
                    }
                    showOnboardingRegionDialog = false
                    showError = false
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Modern visual illustration / Hero section
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.primaryAction.copy(alpha = 0.2f),
                                    colors.primaryAction.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(2.dp, colors.primaryAction.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.primaryAction,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (selectedLanguage == "ru") "VPN Inspector" else "VPN Inspector",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryText,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (selectedLanguage == "ru") "Инструмент проверки сетевого иммунитета" else "Network Immunity Verification Tool",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primaryAction,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (selectedLanguage == "ru") 
                        "Выберите предпочтительный язык интерфейса и целевой регион проверки. Это страна, в которой вы должны находиться без активного подключения к VPN или прокси." 
                        else "Select your preferred app language and the target verification region. This is the country you expect to reside in when no bypass tunnel is active.",
                    fontSize = 13.sp,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Dynamic card containing configurations
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        
                        // Language block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primaryAction,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedLanguage == "ru") "Язык интерфейса" else "App Language",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("ru" to "🇷🇺 Русский", "en" to "🇺🇸 English").forEach { (code, name) ->
                                val isSelected = selectedLanguage == code
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) colors.primaryAction else colors.background)
                                        .border(1.dp, if (isSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                                        .clickable { selectedLanguage = code }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) colors.primaryActionText else colors.primaryText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Target Region block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = colors.primaryAction,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedLanguage == "ru") "Целевой регион проверки" else "Target Verification Region",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simple Grid alternatives
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
                                        .background(if (isSelected) colors.primaryAction.copy(alpha = 0.1f) else colors.background)
                                        .border(1.dp, if (isSelected) colors.primaryAction else colors.border.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            isCustomSelected = false
                                            selectedRegion = code
                                            showError = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = name, color = colors.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            isCustomSelected = false
                                            selectedRegion = code
                                            showError = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = colors.primaryAction,
                                            unselectedColor = colors.secondaryText.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            // Custom Region Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCustomSelected) colors.primaryAction.copy(alpha = 0.1f) else colors.background)
                                    .border(1.dp, if (isCustomSelected) colors.primaryAction else colors.border.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        isCustomSelected = true
                                        showError = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (selectedLanguage == "ru") "🌐 Другой (свой код)" else "🌐 Other (Custom code)",
                                    color = colors.primaryText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                RadioButton(
                                    selected = isCustomSelected,
                                    onClick = {
                                        isCustomSelected = true
                                        showError = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.primaryAction,
                                        unselectedColor = colors.secondaryText.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        // Animate Custom Region block nicely
                        AnimatedVisibility(
                            visible = isCustomSelected,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                OutlinedTextField(
                                    value = customRegionCode,
                                    onValueChange = { input ->
                                        if (input.length <= 2) {
                                            customRegionCode = input.uppercase().filter { it.isLetter() }
                                            showError = false
                                        }
                                    },
                                    label = { Text(if (selectedLanguage == "ru") "Двухбуквенный ISO код страны" else "2-letter ISO country code") },
                                    placeholder = { Text("US") },
                                    trailingIcon = {
                                        IconButton(onClick = { showOnboardingRegionDialog = true }) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = colors.primaryAction)
                                        }
                                    },
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

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (showError) {
                                        Text(
                                            text = if (selectedLanguage == "ru") "Должно состоять ровно из 2 букв" else "Must be exactly 2 letters",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }

                                    TextButton(
                                        onClick = { showOnboardingRegionDialog = true }
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primaryAction)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (selectedLanguage == "ru") "Поиск стран..." else "Search countries...",
                                            color = colors.primaryAction,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
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
                        text = if (selectedLanguage == "ru") "Начать проверку" else "Get Started Check",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionSubScreen(
    currentLang: String,
    currentRegion: String,
    onBack: () -> Unit,
    onRegionSelected: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val isRussian = currentLang == "ru"
    var searchQuery by remember { mutableStateOf("") }

    val popularCountryCodes = remember {
        listOf("RU", "US", "DE", "KZ", "UA", "GB", "FR", "NL", "TR", "CN")
    }

    val allCountryCodes = remember(isRussian) {
        java.util.Locale.getISOCountries().toList()
            .map { code -> code to getCountryName(code, isRussian) }
            .sortedBy { it.second }
    }

    val filteredCountries = remember(searchQuery, allCountryCodes) {
        if (searchQuery.isBlank()) {
            allCountryCodes
        } else {
            allCountryCodes.filter { (code, name) ->
                code.contains(searchQuery, ignoreCase = true) ||
                name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val directCode = searchQuery.trim().uppercase()
    val isDirectCodeValid = directCode.length == 2 && directCode.all { it.isLetter() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
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
                    contentDescription = if (isRussian) "Назад" else "Back",
                    tint = colors.primaryAction
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRussian) "Выбор целевого региона" else "Select Target Region",
                fontSize = 20.sp,
                color = colors.primaryText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isRussian) 
                "Комплексный GeoIP-анализ сопоставляет ваш реальный IP с установленной целевой страной. Отклонение сигнализирует об активности обходных туннелей."
                else "GeoIP analysis matches your active IP with the selected target country. Discrepancies report an active bypass tunnel.",
            color = colors.secondaryText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isRussian) "Поиск страны или ISO код..." else "Search country or ISO code...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.secondaryText) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.secondaryText)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryAction,
                unfocusedBorderColor = colors.border,
                focusedLabelColor = colors.primaryAction,
                unfocusedLabelColor = colors.secondaryText
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Direct ISO match suggestion if typed in search but does not exist in localized name perfectly
            if (isDirectCodeValid && filteredCountries.none { it.first == directCode }) {
                item {
                    val flag = getFlagEmoji(directCode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryAction.copy(alpha = 0.1f))
                            .border(1.dp, colors.primaryAction, RoundedCornerShape(12.dp))
                            .clickable {
                                onRegionSelected(directCode)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(flag, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = if (isRussian) "Использовать код \"$directCode\"" else "Use custom code \"$directCode\"",
                                    color = colors.primaryAction,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (isRussian) "Пользовательский ввод" else "Custom code override",
                                    color = colors.secondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                    }
                }
            }

            // If empty search, render popular countries first
            if (searchQuery.isBlank()) {
                item {
                    Text(
                        text = if (isRussian) "Популярные страны" else "Popular Countries",
                        color = colors.secondaryActionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }

                items(popularCountryCodes) { code ->
                    val isSelected = currentRegion == code
                    val flag = getFlagEmoji(code)
                    val name = getCountryName(code, isRussian)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.primaryAction.copy(alpha = 0.15f) else colors.surface)
                            .border(1.dp, if (isSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                            .clickable {
                                onRegionSelected(code)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(flag, fontSize = 24.sp)
                            Text(
                                name,
                                color = colors.primaryText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRussian) "Все страны" else "All Countries",
                        color = colors.secondaryActionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }

            // All countries list or search results
            items(filteredCountries) { (code, name) ->
                val isSelected = currentRegion == code
                val flag = getFlagEmoji(code)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primaryAction.copy(alpha = 0.15f) else colors.surface)
                        .border(1.dp, if (isSelected) colors.primaryAction else colors.border, RoundedCornerShape(12.dp))
                        .clickable {
                            onRegionSelected(code)
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(flag, fontSize = 24.sp)
                        Text(
                            name,
                            color = colors.primaryText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAction)
                    }
                }
            }

            if (filteredCountries.isEmpty() && !isDirectCodeValid) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRussian) "Ничего не найдено" else "No countries found",
                            color = colors.secondaryText,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
