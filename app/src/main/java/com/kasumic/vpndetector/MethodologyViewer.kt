package com.kasumic.vpndetector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MethodologySection(
    val title: String,
    val content: String
)

fun parseMethodologyMarkdown(text: String): List<MethodologySection> {
    val sections = mutableListOf<MethodologySection>()
    val lines = text.split("\n")
    
    var currentTitle = "Введение"
    val currentContent = java.lang.StringBuilder()
    
    for (line in lines) {
        if (line.startsWith("# ")) {
            if (currentTitle.isNotEmpty() || currentContent.isNotEmpty()) {
                sections.add(MethodologySection(currentTitle, currentContent.toString().trim()))
                currentContent.clear()
            }
            currentTitle = line.removePrefix("# ").trim()
        } else {
            currentContent.append(line).append("\n")
        }
    }
    
    if (currentTitle.isNotEmpty() || currentContent.isNotEmpty()) {
        sections.add(MethodologySection(currentTitle, currentContent.toString().trim()))
    }
    
    // Remove the first empty section if it exists
    return sections.filter { it.content.isNotEmpty() || it.title != "Введение" }
}

sealed class MarkdownElement {
    data class TextElement(val text: androidx.compose.ui.text.AnnotatedString) : MarkdownElement()
    data class TableElement(
        val headers: List<androidx.compose.ui.text.AnnotatedString>, 
        val rows: List<List<androidx.compose.ui.text.AnnotatedString>>
    ) : MarkdownElement()
}

fun parseMarkdownElements(content: String, colors: AppColors): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = content.split("\n")
    
    val currentText = StringBuilder()
    
    var inTable = false
    var tableHeaders = listOf<androidx.compose.ui.text.AnnotatedString>()
    val tableRows = mutableListOf<List<androidx.compose.ui.text.AnnotatedString>>()

    fun flushText() {
        if (currentText.isNotEmpty()) {
            elements.add(MarkdownElement.TextElement(formatMarkdownContent(currentText.toString().trimEnd(), colors)))
            currentText.clear()
        }
    }
    
    for (line in lines) {
        if (line.trim().startsWith("|")) {
            if (!inTable) {
                flushText()
                inTable = true
                val cols = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                tableHeaders = cols.map { formatMarkdownContent(it, colors) }
            } else {
                if (line.contains("---")) continue // Skip separator
                val cols = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (cols.isNotEmpty()) {
                    tableRows.add(cols.map { formatMarkdownContent(it, colors) })
                }
            }
        } else {
            if (inTable) {
                elements.add(MarkdownElement.TableElement(tableHeaders, tableRows.toList()))
                tableHeaders = emptyList()
                tableRows.clear()
                inTable = false
            }
            currentText.append(line).append("\n")
        }
    }
    
    if (inTable) {
        elements.add(MarkdownElement.TableElement(tableHeaders, tableRows.toList()))
    }
    flushText()
    
    return elements
}

fun formatMarkdownContent(text: String, colors: AppColors): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        for ((index, line) in lines.withIndex()) {
            when {
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.primaryText)) {
                        append(line.removePrefix("## "))
                    }
                }
                line.startsWith("- ") -> {
                    append("  • ")
                    appendFormattedLine(line.removePrefix("- "), colors)
                }
                line.startsWith("```") -> {
                    // Ignore code block ticks
                }
                else -> {
                    appendFormattedLine(line, colors)
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendFormattedLine(line: String, colors: AppColors) {
    val parts = line.split("**")
    for ((i, part) in parts.withIndex()) {
        if (i % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.primaryText)) {
                append(part)
            }
        } else {
            // Also check for inline code ticks `
            val codeParts = part.split("`")
            for ((j, cPart) in codeParts.withIndex()) {
                if (j % 2 == 1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = colors.border.copy(alpha = 0.3f))) {
                        append(cPart)
                    }
                } else {
                    append(cPart)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodologyViewerScreen(
    onDismiss: () -> Unit,
    colors: AppColors
) {
    val sections = remember { parseMethodologyMarkdown(MethodologyData.text) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Официальная Методика", color = colors.primaryText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = colors.primaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.primaryText
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.primaryAction.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Методика выявления признаков использования средств обхода блокировок на клиентских устройствах. Распознанный текст в формате Markdown.",
                            color = colors.secondaryText,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Источник: Профсоюз работников IT",
                            color = colors.primaryAction,
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://t.me/ruitunion/893")
                            }.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Исходный код (GitHub)",
                            color = colors.primaryAction,
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://github.com/Kasumicic/vpn-inspector")
                            }.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                }
            }
            items(
                items = sections,
                key = { it.title }
            ) { section ->
                ExpandableMethodologySection(section = section, colors = colors)
            }
        }
    }
}

@Composable
fun ExpandableMethodologySection(section: MethodologySection, colors: AppColors) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = section.title,
                    color = colors.primaryAction,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Развернуть",
                    tint = colors.primaryAction
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    val elements = remember(section.content) { parseMarkdownElements(section.content, colors) }
                    for (element in elements) {
                        when (element) {
                            is MarkdownElement.TextElement -> {
                                Text(
                                    text = element.text,
                                    color = colors.secondaryText,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                            is MarkdownElement.TableElement -> {
                                MarkdownTable(table = element, colors = colors)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownTable(table: MarkdownElement.TableElement, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
    ) {
        // Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryAction.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            table.headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f).padding(4.dp),
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    fontSize = 12.sp
                )
            }
        }
        // Rows
        table.rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) colors.background.copy(alpha = 0.3f) else colors.surface)
                    .padding(8.dp)
            ) {
                row.forEach { cellText ->
                    Text(
                        text = cellText,
                        modifier = Modifier.weight(1f).padding(4.dp),
                        color = colors.secondaryText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
