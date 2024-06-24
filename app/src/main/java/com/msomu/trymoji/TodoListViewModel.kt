package com.msomu.trymoji

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TodoListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    fun addTask(taskName: String) {
        val task = TodoTask(
            task = taskName,
        )
        var todoTasks = emptyList<TodoTask>()
        _uiState.update { currentState ->
            todoTasks = currentState.todoTasks + task
            currentState.copy(
                todoTasks = todoTasks, currentTask = ""
            )
        }
        val emoji = getEmoji(task.task)
        emoji?.let {
            val bitmap = emojiToBitmap(emoji, 120)
            val colors = getPalatteColor(bitmap)
            val currentTask = todoTasks.find { it.task == taskName }
            currentTask?.emoji = emoji
            currentTask?.bitmap = bitmap
            currentTask?.pColor = colors.first
            currentTask?.bgColor = colors.second
            _uiState.update { currentState ->
                currentState.copy(
                    todoTasks = todoTasks,
                )
            }
        }
    }

    private fun getPalatteColor(bitmap: Bitmap): Pair<Color?,Color?> {
        val palette = Palette.from(bitmap).generate()
        val pColor = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
        val bgColor = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
        return Pair(pColor, bgColor)
    }

    private fun getEmoji(task: String): String? {
        return null
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
    val todoTasks: List<TodoTask> = emptyList(),
    val currentTask: String = ""
)

data class TodoTask(
    val task: String,
    val status: Boolean = false,
    var emoji: String? = null,
    var bitmap: Bitmap? = null,
    var bgColor: Color? = null,
    var pColor: Color? = null,
    val syncStatus: SyncStatus = SyncStatus.ADDING
)

enum class SyncStatus {
    ADDING, ADDED
}