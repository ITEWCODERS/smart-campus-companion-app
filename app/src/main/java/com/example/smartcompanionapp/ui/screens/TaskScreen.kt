package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.domain.TaskIntent
import com.example.smartcompanionapp.domain.TaskUiState
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Tasks", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
    )
}

// ─────────────────────────────────────────────
// MAIN TASK SCREEN
// ─────────────────────────────────────────────
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    // ── STATE DRIVEN LIST RENDERING ──────────────────────────────────────────
    // collectAsState() turns the ViewModel's StateFlow into Compose State.
    // Every time the ViewModel emits a new TaskUiState, the UI automatically
    // recomposes — we never manually refresh or invalidate the list.
    val uiState by viewModel.uiState.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = { TaskTopBar { navController.popBackStack() } },
        bottomBar = { CampusBottomNav(navController) },
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── STATE DRIVEN LIST RENDERING ───────────────────────────────────
            // The UI reacts to the current state: Loading → show spinner,
            // Success → show list or empty message, Error → show error text.
            // No manual list management needed — the state drives everything.
            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TaskUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("There are no tasks yet")
                        }
                    } else {
                        // Renders the task list from the immutable state.tasks list.
                        // LazyColumn only recomposes items that changed.
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            items(state.tasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    // ── INTENT HANDLING ───────────────────────────────────────────
                                    // Instead of calling viewModel.deleteTask() directly,
                                    // the UI sends a TaskIntent. The ViewModel decides what to do.
                                    onDelete = {
                                        viewModel.processIntent(TaskIntent.DeleteTask(task))
                                    },
                                    onEdit = {
                                        selectedTask = task
                                        showEditTaskDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                is TaskUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // ── ADD TASK DIALOG ───────────────────────────────────────────────────
        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAddTask = { title, description, subject, date, dueDate ->
                    // ── INTENT HANDLING ───────────────────────────────────────
                    // Wraps all user input into a TaskIntent.AddTask and sends it
                    // to the ViewModel. The UI has no business logic — it just fires intents.
                    viewModel.processIntent(
                        TaskIntent.AddTask(title, description, subject, date, dueDate)
                    )
                    showAddTaskDialog = false
                }
            )
        }

        // ── EDIT TASK DIALOG ──────────────────────────────────────────────────
        if (showEditTaskDialog) {
            selectedTask?.let { task ->
                EditTaskDialog(
                    task = task,
                    onDismiss = { showEditTaskDialog = false },
                    onEditTask = { title, description, subject, date, dueDate ->
                        // ── INTENT HANDLING ───────────────────────────────────
                        // Wraps edit data into TaskIntent.UpdateTask.
                        viewModel.processIntent(
                            TaskIntent.UpdateTask(task, title, description, subject, date, dueDate)
                        )
                        showEditTaskDialog = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// DATE HELPER
// ─────────────────────────────────────────────
// Converts the Long milliseconds from DatePicker into a readable "dd/MM/yyyy" string
private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

// ─────────────────────────────────────────────
// ADD TASK DIALOG
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    // Now takes 5 parameters: title, description, subject, date, dueDate
    onAddTask: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

    // ── DATE PICKER FOR DATE (scheduled/added date) ────────────────────────
    var date by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // ── DATE PICKER FOR DUE DATE (deadline) ───────────────────────────────
    var dueDate by remember { mutableStateOf("") }
    var showDueDatePicker by remember { mutableStateOf(false) }
    val dueDatePickerState = rememberDatePickerState()

    // DATE picker dialog — for the scheduled/added date
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = convertMillisToDate(it) // Convert millis → "dd/MM/yyyy"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DUE DATE picker dialog — for the deadline
    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDatePickerState.selectedDateMillis?.let {
                        dueDate = convertMillisToDate(it)
                    }
                    showDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── DATE PICKER FOR DATE ─────────────────────────────────────
                // Read-only field — clicking the calendar icon opens the DatePicker dialog.
                // The selected date is stored as a "dd/MM/yyyy" string in `date` state.
                // ScheduleScreen uses this value to filter tasks for a given day.
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Scheduled Date (dd/MM/yyyy)") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.DateRange,
                            contentDescription = "Select scheduled date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── DATE PICKER FOR DUE DATE ─────────────────────────────────
                // Same pattern — opens a separate DatePicker for the deadline.
                // This value is shown as "Due: ..." on the TaskCard.
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {},
                    label = { Text("Due Date (dd/MM/yyyy)") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.DateRange,
                            contentDescription = "Select due date",
                            modifier = Modifier.clickable { showDueDatePicker = true }
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Only submit if required fields are filled
                    if (title.isNotBlank() && date.isNotBlank() && dueDate.isNotBlank()) {
                        onAddTask(title, description, subject, date, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UniPrimary)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface)
            ) {
                Text("Cancel", color = TextPrimary)
            }
        }
    )
}

// ─────────────────────────────────────────────
// EDIT TASK DIALOG
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    // Now takes 5 parameters: title, description, subject, date, dueDate
    onEditTask: (String, String, String, String, String) -> Unit
) {
    // Pre-fill all fields with the existing task's values
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var subject by remember { mutableStateOf(task.subject) }

    // ── DATE PICKER FOR DATE ──────────────────────────────────────────────
    var date by remember { mutableStateOf(task.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // ── DATE PICKER FOR DUE DATE ──────────────────────────────────────────
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    val dueDatePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = convertMillisToDate(it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDatePickerState.selectedDateMillis?.let {
                        dueDate = convertMillisToDate(it)
                    }
                    showDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── DATE PICKER FOR DATE ─────────────────────────────────────
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Scheduled Date (dd/MM/yyyy)") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.DateRange,
                            contentDescription = "Select scheduled date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── DATE PICKER FOR DUE DATE ─────────────────────────────────
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {},
                    label = { Text("Due Date (dd/MM/yyyy)") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.DateRange,
                            contentDescription = "Select due date",
                            modifier = Modifier.clickable { showDueDatePicker = true }
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank() && dueDate.isNotBlank()) {
                        onEditTask(title, description, subject, date, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UniPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface)
            ) {
                Text("Cancel", color = TextPrimary)
            }
        }
    )
}

// ─────────────────────────────────────────────
// TASK CARD
// ─────────────────────────────────────────────
// Displays a single task. Receives task data and callbacks — has no logic of its own.
// The list of these is rendered by LazyColumn in TaskScreen based on the current UiState.
@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(UniAccent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Shows the scheduled date (when it was added/scheduled)
                Text(
                    text = "Scheduled: ${task.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Shows the deadline
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}