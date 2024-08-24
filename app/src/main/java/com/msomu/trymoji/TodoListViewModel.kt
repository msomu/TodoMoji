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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private lateinit var chat: Chat

    init {
        viewModelScope.launch {

        }
    }

    fun addTask(taskName: String) {
        val task = TodoTask(
            task = taskName,
        )
        var todoTasks: List<TodoTask>
        _uiState.update { currentState ->
            todoTasks = currentState.todoTasks + task
            currentState.copy(
                todoTasks = todoTasks.toMutableList(), currentTask = ""
            )
        }
        viewModelScope.launch {
            val emoji = getEmoji(task.task)
            Log.d("TodoListViewModel", "Got this emoji $emoji")
            if(emoji == null){
                _uiState.value = _uiState.value.copy(
                    todoTasks = _uiState.value.todoTasks.map { task ->
                        if (task.task == taskName) {
                            task.copy(
                                syncStatus = SyncStatus.ADDED
                            )
                        } else {
                            task
                        }
                    }.toMutableList()
                )
            }else{
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