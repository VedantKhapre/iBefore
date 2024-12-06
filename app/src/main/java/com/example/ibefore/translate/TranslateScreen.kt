package com.example.ibefore.feature.translate

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ibefore.translate.TranslateViewModel
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    navController: NavController,
    translateViewModel: TranslateViewModel = hiltViewModel()
) {
    val translatedText by translateViewModel.translatedText.collectAsState()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var sourceLanguage by remember { mutableStateOf("English") }

    val languages = listOf("Spanish","German", "English", "Hindi", "Bengali", "Tamil")
    var targetLanguage by remember { mutableStateOf(languages[0]) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(inputText, targetLanguage) {
        if (inputText.text.isNotEmpty()) {
            val result = translateViewModel.translateMessage(
                inputText.text, sourceLanguage, targetLanguage
            )
            translateViewModel.updateTranslatedText(result)
        }
    }

    val primaryColor = Color(0xFF007AFF)
    val backgroundColor = Color(0xFF121212)
    val surfaceColor = Color(0xFF1E1E1E)
    val textColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Offline Translator",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        if (it.text.isEmpty()) {
                            translateViewModel.updateTranslatedText("")
                        }
                    },
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            if (inputText.text.isEmpty()) {
                                Text(
                                    text = "Enter text to translate",
                                    color = Color.Gray,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )

                if (inputText.text.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(backgroundColor.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(color = primaryColor)
                            ) {
                                inputText = TextFieldValue("")
                                translateViewModel.updateTranslatedText("")
                                focusManager.clearFocus()
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Input",
                            tint = textColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    TextField(
                        value = targetLanguage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Translate to", color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Language",
                                tint = primaryColor
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = surfaceColor,
                            focusedContainerColor = surfaceColor,
                            unfocusedTextColor = textColor,
                            focusedTextColor = textColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = primaryColor,
                            unfocusedTrailingIconColor = textColor,
                            focusedTrailingIconColor = primaryColor
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(surfaceColor)
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        language,
                                        color = if (language == targetLanguage) primaryColor else textColor
                                    )
                                },
                                onClick = {
                                    targetLanguage = language
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = textColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = translatedText.isNotEmpty(),
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessLow))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                        .border(
                            BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = translatedText.ifEmpty { "Translation will appear here" },
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, translatedText)
                                    Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                ),
                                border = BorderStroke(1.dp, primaryColor),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy", style = TextStyle(fontSize = 14.sp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Translated Text", text)
    clipboard?.setPrimaryClip(clip)
}