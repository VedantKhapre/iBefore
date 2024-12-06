package com.example.ibefore.feature.chat

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ibefore.model.Message
import com.example.ibefore.translate.TranslateViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.rounded.AttachFile

@SuppressLint("NewApi")
@Composable
fun ChatScreen(
    navController: NavController,
    channelId: String,
    channelName: String
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val translateViewModel: TranslateViewModel = hiltViewModel()
    val context = LocalContext.current

    val chooserDialog = remember { mutableStateOf(false) }
    val messages = viewModel.message.collectAsState()
    val translatedMessages =
        remember { mutableStateOf(messages.value) }
    val isTranslationActive = remember { mutableStateOf(false) }
    val selectedLanguage = remember { mutableStateOf("English") }
    val coroutineScope = rememberCoroutineScope()
    val isModelDownloaded by translateViewModel.isModelDownloaded.collectAsState()

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }


    val cameraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let {
                viewModel.sendImageMessage(it, channelId)
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImageMessage(it, channelId) }
    }

    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = ContextCompat.getExternalFilesDirs(
            navController.context, Environment.DIRECTORY_PICTURES
        ).first()
        return FileProvider.getUriForFile(
            navController.context,
            "${navController.context.packageName}.provider",
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                cameraImageUri.value = Uri.fromFile(this)
            }
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                cameraImageLauncher.launch(createImageUri())
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LaunchedEffect(key1 = true) {
            viewModel.listenForMessages(channelId)
        }
        val selectedSourceLanguage = remember { mutableStateOf("Spanish") }

        ChannelHeader(
            channelName = channelName,
            isTranslationActive = isTranslationActive.value,
            selectedLanguage = selectedLanguage.value,
            selectedSourceLanguage = selectedSourceLanguage.value,
            onLanguageChange = { language -> selectedLanguage.value = language },
            onSourceLanguageChange = { language -> selectedSourceLanguage.value = language },
            onTranslateToggle = {
                if (isModelDownloaded) {
                    isTranslationActive.value = !isTranslationActive.value
                    coroutineScope.launch {
                        if (isTranslationActive.value) {
                            val translatedList = messages.value.map { message ->
                                if (message.message != null) {
                                    try {
                                        val translatedText = translateViewModel.translateMessage(
                                            inputText = message.message,
                                            sourceLang = selectedSourceLanguage.value,
                                            targetLang = selectedLanguage.value
                                        )
                                        message.copy(message = translatedText)
                                    } catch (e: Exception) {
                                        message
                                    }
                                } else message
                            }
                            translatedMessages.value = translatedList
                        } else {
                            translatedMessages.value = messages.value
                        }
                    }
                } else {
                    Toast.makeText(context, "Translation model is downloading", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )

        ChatMessages(
            messages = if (isTranslationActive.value) translatedMessages.value else messages.value,
            onSendMessage = { message -> viewModel.sendMessage(channelId, message) },
            onImageClicked = { chooserDialog.value = true }
        )
    }

    if (chooserDialog.value) {
        ContentSelectionDialog(
            onCameraSelected = {
                chooserDialog.value = false
                if (navController.context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    cameraImageLauncher.launch(createImageUri())
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }, onGallerySelected = {
                chooserDialog.value = false
                imageLauncher.launch("image/*")
            })
    }
}

@Composable
fun ChannelHeader(
    channelName: String,
    isTranslationActive: Boolean,
    selectedLanguage: String,
    selectedSourceLanguage: String,
    onLanguageChange: (String) -> Unit,
    onSourceLanguageChange: (String) -> Unit,
    onTranslateToggle: () -> Unit
) {

    val languages = listOf("Spanish", "German", "English", "Hindi", "Bengali", "Tamil")

    var targetLanguageExpanded by remember { mutableStateOf(false) }
    var sourceLanguageExpanded by remember { mutableStateOf(false) }

    val primaryBackground = Color(0xFF1E1E1E)
    val secondaryBackground = Color(0xFF2C2C3E)
    val accentColor = Color(0xFF4A90E2)
    val textColor = Color.White
    val translationActiveColor = Color(0xFF34D058)
    val translationInactiveColor = Color(0xFFE53030)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryBackground)
            .padding(16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = channelName,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )

            Button(
                onClick = onTranslateToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTranslationActive) translationActiveColor else translationInactiveColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = if (isTranslationActive) "Original" else "Translate",
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val dropdownWidth = maxWidth

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(secondaryBackground, shape = RoundedCornerShape(12.dp))
                            .clickable { sourceLanguageExpanded = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedSourceLanguage,
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Source Language",
                            tint = textColor.copy(alpha = 0.7f)
                        )
                    }
                    DropdownMenu(
                        expanded = sourceLanguageExpanded,
                        onDismissRequest = { sourceLanguageExpanded = false },
                        modifier = Modifier
                            .width(dropdownWidth)
                            .heightIn(max = 200.dp)
                            .background(
                                color = Color(0xFF3A3A4E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        languages.take(3).forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSourceLanguageChange(language)
                                    sourceLanguageExpanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (language == selectedSourceLanguage)
                                            accentColor.copy(alpha = 0.2f)
                                        else
                                            Color.Transparent
                                    )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        Column(
                            modifier = Modifier
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            languages.drop(3).forEach { language ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = language,
                                            color = textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSourceLanguageChange(language)
                                        sourceLanguageExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (language == selectedSourceLanguage)
                                                accentColor.copy(alpha = 0.2f)
                                            else
                                                Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val dropdownWidth = maxWidth

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(secondaryBackground, shape = RoundedCornerShape(12.dp))
                            .clickable { targetLanguageExpanded = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedLanguage,
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Target Language",
                            tint = textColor.copy(alpha = 0.7f)
                        )
                    }

                    DropdownMenu(
                        expanded = targetLanguageExpanded,
                        onDismissRequest = { targetLanguageExpanded = false },
                        modifier = Modifier
                            .width(dropdownWidth)
                            .heightIn(max = 200.dp)
                            .background(
                                color = Color(0xFF3A3A4E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        languages.take(3).forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onLanguageChange(language)
                                    targetLanguageExpanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (language == selectedLanguage)
                                            accentColor.copy(alpha = 0.2f)
                                        else
                                            Color.Transparent
                                    )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        Column(
                            modifier = Modifier
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            languages.drop(3).forEach { language ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = language,
                                            color = textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onLanguageChange(language)
                                        targetLanguageExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (language == selectedLanguage)
                                                accentColor.copy(alpha = 0.2f)
                                            else
                                                Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ChatMessages(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onImageClicked: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C2D34))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onImageClicked) {
                Icon(
                    imageVector = Icons.Rounded.AttachFile,
                    contentDescription = "Attach",
                    tint = Color(0xFFFFFFFF)
                )
            }

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = {
                    Text(
                        text = "Type a message...",
                        color = Color(0xFFFFFFFF)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2C2D34),
                    unfocusedContainerColor = Color(0xFF2C2D34),
                    focusedTextColor = Color(0xFFFFFFFF),
                    unfocusedTextColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank())
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFFFFFFF)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val backgroundColor = if (isCurrentUser)
        Color(0xFF4CAF50)
    else
        Color(0xFF2C2D34)

    val textColor = Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) {
            if (!isCurrentUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = message.message ?: "",
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun ContentSelectionDialog(onCameraSelected: () -> Unit, onGallerySelected: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onCameraSelected) {
                Text(text = "Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = onGallerySelected) {
                Text(text = "Gallery")
            }
        },
        title = { Text(text = "Select your source?") },
        text = { Text(text = "Would you like to pick an image from the gallery or use the camera") }
    )
}
