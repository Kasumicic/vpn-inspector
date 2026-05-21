package com.example

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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.ScanResult
import com.example.ui.theme.MyApplicationTheme

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            val colors = if (isDarkTheme) DarkAppColors else LightAppColors

            CompositionLocalProvider(LocalAppColors provides colors) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colors.background
                    ) {
                        MainScreen(isDarkTheme, { isDarkTheme = it })
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
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
                    icon = { Icon(Icons.Default.Search, contentDescription = "Проверка") },
                    label = { Text("Проверка") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.navSelectedIcon,
                        selectedTextColor = colors.navSelectedText,
                        indicatorColor = colors.navIndicator,
                        unselectedIconColor = colors.navUnselected,
                        unselectedTextColor = colors.navUnselected
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                    label = { Text("Настройки и Инфо") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
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
                1 -> SettingsAndAboutScreen(isDarkTheme, onThemeChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndAboutScreen(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val colors = LocalAppColors.current
    var showMethodology by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryAction, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("VPN Inspector", fontSize = 24.sp, color = colors.primaryText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Версия 1.1", textAlign = TextAlign.Center, color = colors.secondaryText)
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Тёмная тема", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("О приложении", color = colors.secondaryActionText, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Приложение создано с целью демонстрации способов обнаружения VPN и Proxy согласно официально утвержденной методике. Инструмент призван показать, что для сокрытия факта обхода необходим комплексный подход и обход целого ряда независимых проверок.",
            color = colors.secondaryText, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Ключевые пункты методики:", color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            MethodologyBullet("• GeoIP", "Анализ на стороне сервера. Сравнение IP с репутационными базами.", colors)
            MethodologyBullet("• Прямые признаки", "Опрос системного API Android на наличие VPN транспорта и поиск установленных VPN-пакетов.", colors)
            MethodologyBullet("• Интерфейсы", "Поиск виртуальных адаптеров туннелирования (tun, tap, wg).", colors)
            MethodologyBullet("• Аномалии MTU", "Анализ размера кадра (MTU) на следы инкапсуляции VPN-заголовков.", colors)
            MethodologyBullet("• Локальные Proxy", "Проверка стандартных портов, открываемых клиентскими proxy.", colors)
            MethodologyBullet("• Подмена IP", "Проверка выдачи фейковых локальных IP-адресов доменам (Fake-IP туннелирование).", colors)
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
            Text("Читать Методичку", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(modifier = Modifier.height(24.dp))

        Text("Разработчик", color = colors.secondaryActionText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
    }

    if (showMethodology) {
        AlertDialog(
            onDismissRequest = { showMethodology = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxSize().padding(16.dp),
            containerColor = colors.background,
            title = {
                Text("Официальная Методика", color = colors.primaryText, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(MethodologyData.text, color = colors.secondaryText, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodology = false }) {
                    Text("Закрыть", color = colors.secondaryActionText, fontWeight = FontWeight.Bold)
                }
            }
        )
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

        ScanButton(
            isScanning = uiState.isScanning,
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
                Text("Описание:", fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                Text(result.description, color = colors.secondaryText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Показания:", fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                Text(result.details, color = if(result.isRisky) colors.error else colors.success, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (result.isRisky && result.fixSuggestion.isNotEmpty()) {
                    Text("Как обмануть детект:", fontWeight = FontWeight.Bold, color = colors.secondaryActionText)
                    Text(result.fixSuggestion, color = colors.primaryText, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = colors.secondaryActionText)
            }
        }
    )
}

@Composable
fun HeaderSection(ipAddress: String) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "VPN INSPECTOR",
            color = colors.secondaryActionText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Текущий IP: $ipAddress",
            color = colors.secondaryText,
            fontSize = 16.sp
        )
    }
}

@Composable
fun StatusCard(uiState: ScannerUiState) {
    val colors = LocalAppColors.current

    val statusText = when {
        uiState.isScanning -> "Идет сканирование..."
        !uiState.scanCompleted -> "Система готова.\nНажмите для анализа."
        uiState.decision == DecisionState.DETECTED -> "ОБНАРУЖЕН ОБХОД"
        uiState.decision == DecisionState.NEEDS_CHECK -> "ТРЕБУЕТСЯ ПРОВЕРКА"
        else -> "СИСТЕМА ЧИСТА"
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
        items(results) { result ->
            ResultItem(result, onResultClick)
        }
    }
}

@Composable
fun ResultItem(result: ScanResult, onClick: (ScanResult) -> Unit) {
    val colors = LocalAppColors.current
    val iconColor = if (result.isRisky) colors.error else colors.success
    val icon = if (result.isRisky) Icons.Default.Warning else Icons.Default.CheckCircle

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

@Composable
fun ScanButton(isScanning: Boolean, onScanClick: () -> Unit) {
    val colors = LocalAppColors.current
    Button(
        onClick = onScanClick,
        enabled = !isScanning,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primaryAction,
            contentColor = colors.primaryActionText,
            disabledContainerColor = colors.border,
            disabledContentColor = colors.secondaryText
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = if (isScanning) "Анализ в процессе..." else "ЗАПУСТИТЬ ПРОВЕРКУ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

