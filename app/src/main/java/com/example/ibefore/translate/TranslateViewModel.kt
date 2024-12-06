package com.example.ibefore.translate

import androidx.lifecycle.ViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TranslateViewModel : ViewModel() {
    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isModelDownloaded = MutableStateFlow(false)
    val isModelDownloaded: StateFlow<Boolean> = _isModelDownloaded.asStateFlow()

    private val languageMap = mapOf(
        "Spanish" to TranslateLanguage.SPANISH,
        "German" to TranslateLanguage.GERMAN,
        "English" to TranslateLanguage.ENGLISH,
        "Hindi" to TranslateLanguage.HINDI,
        "Bengali" to TranslateLanguage.BENGALI,
        "Tamil" to TranslateLanguage.TAMIL
    )

    init {
        preDownloadModels()
    }

    private fun preDownloadModels() {
        viewModelScope.launch {
            try {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.HINDI)
                    .build()

                val translator = Translation.getClient(options)
                translator.downloadModelIfNeeded().await()
                _isModelDownloaded.update { true }
            } catch (e: Exception) {
                _isModelDownloaded.update { false }
            }
        }
    }

    suspend fun translateMessage(
        inputText: String,
        sourceLang: String = "English",
        targetLang: String
    ): String {
        if (inputText.isBlank()) return inputText

        val sourceLanguage = languageMap[sourceLang]
        val targetLanguage = languageMap[targetLang]

        if (sourceLanguage == null || targetLanguage == null) {
            return "Invalid language selection"
        }

        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

            val translator: Translator = Translation.getClient(options)

            translator.downloadModelIfNeeded().await()
            translator.translate(inputText).await()
        } catch (e: Exception) {
            "Translation error: ${e.localizedMessage ?: "Unknown error occurred"}"
        }
    }

    fun updateTranslatedText(text: String) {
        _translatedText.value = text
    }
}