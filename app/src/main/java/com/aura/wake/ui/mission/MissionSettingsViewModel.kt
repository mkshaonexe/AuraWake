package com.aura.wake.ui.mission

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aura.wake.data.model.Difficulty
import com.aura.wake.data.model.MathMissionConfig
import com.aura.wake.data.model.QrMissionConfig
import com.aura.wake.data.model.TypingMissionConfig
import com.aura.wake.data.repository.SettingsRepository

class MissionSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Math Mission State
    var mathDifficulty by mutableStateOf(Difficulty.MEDIUM)
    var mathProblemCount by mutableStateOf(3)
    
    // Typing Mission State
    var typingSentences by mutableStateOf<List<String>>(emptyList())
    var typingWordCount by mutableStateOf(5)
    
    // QR Mission State
    var qrContent by mutableStateOf<String?>(null)
    var qrLabel by mutableStateOf<String?>(null)

    // Memory Mission State
    var memoryDifficulty by mutableStateOf(Difficulty.MEDIUM)
    var memoryQuestionCount by mutableStateOf(3)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val mathConfig = settingsRepository.getMathMissionConfig()
        mathDifficulty = mathConfig.difficulty
        mathProblemCount = mathConfig.problemCount
        
        val typingConfig = settingsRepository.getTypingMissionConfig()
        typingSentences = typingConfig.sentences
        typingWordCount = typingConfig.wordCount
        
        val qrConfig = settingsRepository.getQrMissionConfig()
        qrContent = qrConfig.qrContent
        qrLabel = qrConfig.qrLabel

        val memoryConfig = settingsRepository.getMemoryMissionConfig()
        memoryDifficulty = memoryConfig.difficulty
        memoryQuestionCount = memoryConfig.questionCount
    }

    fun saveMathSettings(difficulty: Difficulty, count: Int) {
        mathDifficulty = difficulty
        mathProblemCount = count
        settingsRepository.saveMathMissionConfig(MathMissionConfig(difficulty, count))
    }

    fun saveTypingSettings(sentences: List<String>, wordCount: Int) {
        typingSentences = sentences
        typingWordCount = wordCount
        settingsRepository.saveTypingMissionConfig(TypingMissionConfig(sentences, wordCount))
    }

    fun saveQrSettings(content: String, label: String) {
        qrContent = content
        qrLabel = label
        settingsRepository.saveQrMissionConfig(QrMissionConfig(content, label))
    }

    fun saveMemorySettings(difficulty: Difficulty, count: Int) {
        memoryDifficulty = difficulty
        memoryQuestionCount = count
        settingsRepository.saveMemoryMissionConfig(com.aura.wake.data.model.MemoryMissionConfig(difficulty, count))
    }
}
