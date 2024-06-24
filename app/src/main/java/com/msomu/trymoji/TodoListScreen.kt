package com.msomu.trymoji

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    todoListViewModel: TodoListViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val taskUiState by todoListViewModel.uiState.collectAsState()

    Scaffold(modifier = Modifier.systemBarsPadding(), topBar = {
        TopAppBar(title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu"
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp),
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search"
                    )
                    ProfilePicture(
                        Modifier
                            .padding(12.dp)
                            .size(32.dp)
                    )
                }
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(containerColor = Color.Blue,
            contentColor = Color.White,
            shape = CircleShape,
            onClick = {
                showBottomSheet = true
            }) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Task")
        }
    }) {
        TodoList(Modifier.padding(it), taskUiState.todoTasks)
        BottomSheet(
            Modifier.fillMaxWidth(),
            sheetState,
            showBottomSheet,
            taskUiState.currentTask,
            { taskName -> todoListViewModel.editCurrentTask(taskName) },
            {taskName -> todoListViewModel.addTask(taskName)}) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showBottomSheet = false
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier,
    modalBottomSheetState: SheetState,
    showBottomSheet: Boolean,
    currentTask: String,
    updateTask: (String) -> Unit,
    submitTask : (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = { onDismiss() },
            sheetState = modalBottomSheetState,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Task", modifier = Modifier.fillMaxWidth()
                )
                TextField(value = currentTask,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = updateTask,
                    label = { Text(text = "Task Name") },
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitTask(currentTask)
                            onDismiss()
                        }
                    )
                    )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "",
                        modifier = Modifier.clickable {
                            submitTask(currentTask)
                            onDismiss()
                        })
                }
            }
        }
    }
}

@Composable
private fun ProfilePicture(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.clip(CircleShape),
        painter = painterResource(id = R.drawable.somu),
        contentDescription = "profile picture",
        contentScale = ContentScale.Crop
    )
}

@Composable
fun TodoList(modifier: Modifier, todoTasks: List<TodoTask>) {
    LazyColumn(
        modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "23 jun",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp),
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter"
                )
            }
        }
        items(todoTasks) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier,
                        selected = it.status,
                        onClick = { /*TODO*/ },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = it.bgColor ?: Color.Gray,
                            unselectedColor = it.bgColor ?: Color.Gray,
                            disabledSelectedColor = it.bgColor ?: Color.Gray,
                            disabledUnselectedColor = it.bgColor ?: Color.Gray,
                        )
                    )
                    Text(
                        text = it.task, color = Color.Black
                    )
                }
                it.bitmap?.let { bitmap ->
                    Box {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = it.emoji,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    color = it.bgColor ?: Color.Gray
                                )
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
