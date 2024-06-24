package com.msomu.trymoji

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    val model = GenerativeModel(
        "gemini-1.5-flash",
        // Retrieve API key as an environmental variable defined in a Build Configuration
        // see https://github.com/google/secrets-gradle-plugin for further instructions
        BuildConfig.apiKey,
        generationConfig = generationConfig {
            temperature = 1f
            topK = 64
            topP = 0.95f
            maxOutputTokens = 8192
        },
        // safetySettings = Adjust safety settings
        // See https://ai.google.dev/gemini-api/docs/safety-settings
    )

    private lateinit var chat: Chat

    init {
        viewModelScope.launch {
            val chatHistory = listOf(
                content("user") {
                    text("Task: Will be given as an Input to you\n\nInstructions:\n\n1. Analyze the task's core meaning and purpose.\n2. Identify the primary category or theme (e.g., work, personal, health, creative).\n3. Select a single emoji that BEST represents:\n    * The task's action (e.g., 🏋️ for \"workout\")\n    * The task's result or goal (e.g., 🏆 for \"win competition\")\n    * The task's overall feeling or tone (e.g., 😊 for \"enjoyable activity\")\n4. Prioritize clarity and universality of the emoji's meaning.\n5. Avoid emojis that are overly obscure or ambiguous.\n\nOutput: Single emoji ")
                },
                content("model") {
                    text("Okay, I'm ready! Please provide me with the task you want me to analyze. \n")
                },
                content("user") {
                    text("Exercise")
                },
                content("model") {
                    text("🏋️‍ \n")
                },
                content("user") {
                    text("Breakfast")
                },
                content("model") {
                    text("🍳 \n")
                },
                content("user") {
                    text("Lunch")
                },
                content("model") {
                    text("🍽️ \n")
                },
                content("user") {
                    text("Dispose Trash")
                },
                content("model") {
                    text("🗑️ \n")
                },
                content("user") {
                    text("Clean the Desk")
                },
                content("model") {
                    text("🧽 \n")
                },
                content("user") {
                    text("Reply to emails")
                },
                content("model") {
                    text("📧 \n")
                },
                content("user") {
                    text("Buy Apple")
                },
                content("model") {
                    text("🍎 \n\n\n")
                },
                content("user") {
                    text("You should always send only one emoji as a response")
                },
                content("model") {
                    text("Understood! I will ensure to only send one emoji as a response from now on. 😄 \n")
                },)
            chat = model.startChat(chatHistory)
        }
    }

    fun addTask(taskName: String) {
        val task = TodoTask(
            task = taskName,
        )
        var todoTasks = emptyList<TodoTask>()
        _uiState.update { currentState ->
            todoTasks = currentState.todoTasks + task
            currentState.copy(
                todoTasks = todoTasks.toMutableList(), currentTask = ""
            )
        }
        viewModelScope.launch {
            val emoji = getEmoji(task.task)
            Log.d("TodoListViewModel", "Got this emoji $emoji")
            emoji?.let {
                val bitmap = emojiToBitmap(emoji, 120)
                val colors = getPalatteColor(bitmap)

                _uiState.value = _uiState.value.copy(
                    todoTasks = _uiState.value.todoTasks.map { task ->
                        if (task.task == taskName) {
                            task.copy(
                                emoji = emoji,
                                bitmap = bitmap,
                                pColor = colors.first,
                                bgColor = colors.second,
                                syncStatus = SyncStatus.ADDED
                            )
                        } else {
                            task
                        }
                    }.toMutableList()
                )
            }
        }
    }

    private fun getPalatteColor(bitmap: Bitmap): Pair<Color?, Color?> {
        val palette = Palette.from(bitmap).generate()
        val pColor = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
        val bgColor = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
        return Pair(pColor, bgColor)
    }

    private suspend fun getEmoji(task: String): String? {
        return try {
            val ans = chat.sendMessage(task).text
            Log.d("SOMU","Gemini sends this: $ans")
               ans
        } catch (e: Exception) {
            null
        }
    }

    fun editCurrentTask(taskName: String) {
        _uiState.update { currentState ->
            currentState.copy(currentTask = taskName)
        }
    }

    private fun emojiToBitmap(emoji: String, size: Int): Bitmap {
        val textPaint = TextPaint().apply {
            textSize = size.toFloat()
        }

        val textBounds = Rect()
        textPaint.getTextBounds(emoji, 0, emoji.length, textBounds)

        val bitmap =
            Bitmap.createBitmap(textBounds.width(), textBounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Center the emoji vertically
        val xPos = 0f
        val yPos = (canvas.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)

        canvas.drawText(emoji, xPos, yPos, textPaint)

        return bitmap
    }
}

data class TodoListUiState(
    val todoTasks: MutableList<TodoTask> = mutableListOf(),
    val currentTask: String = ""
)

data class TodoTask(
    val task: String,
    val status: Boolean = false,
    val emoji: String? = null,
    val bitmap: Bitmap? = null,
    val bgColor: Color? = null,
    val pColor: Color? = null,
    val syncStatus: SyncStatus = SyncStatus.ADDING
)

enum class SyncStatus {
    ADDING, ADDED
}