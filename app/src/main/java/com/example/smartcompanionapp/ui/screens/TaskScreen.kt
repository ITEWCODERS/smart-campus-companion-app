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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
// TASK LIST HEADER (IMPROVED UI)
// ─────────────────────────────────────────────
@Composable
fun TaskListHeader(taskCount: Int) {
    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = "$taskCount ${if (taskCount == 1) "Item" else "Items"}",
                style = MaterialTheme.typography.bodyMedium,
                color = UniPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtle divider line
        HorizontalDivider(
            color = TextSecondary.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}

// ─────────────────────────────────────────────
// MAIN TASK SCREEN
// ─────────────────────────────────────────────
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
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
                shape = CircleShape,
                containerColor = UniPrimary,
                contentColor = Color.White
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
                            Text(
                                text = "There are no tasks yet",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            item {
                                TaskListHeader(taskCount = state.tasks.size)
                            }
                            items(state.tasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
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
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAddTask = { title, description, subject, date, dueDate ->
                    viewModel.processIntent(
                        TaskIntent.AddTask(title, description, subject, date, dueDate)
                    )
                    showAddTaskDialog = false
                }
            )
        }

        if (showEditTaskDialog) {
            selectedTask?.let { task ->
                EditTaskDialog(
                    task = task,
                    onDismiss = { showEditTaskDialog = false },
                    onEditTask = { title, description, subject, date, dueDate ->
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
private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    // Error states
    var titleError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var dueDateError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var showDueDatePicker by remember { mutableStateOf(false) }
    val dueDatePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = convertMillisToDate(it)
                        dateError = false
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
                        dueDateError = false
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = UniPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Create New Task")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; if (it.isNotBlank()) titleError = false },
                    label = { Text("Task Title *") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; if (it.isNotBlank()) subjectError = false },
                    label = { Text("Subject / Category *") },
                    placeholder = { Text("e.g. Math, Research, Work") },
                    isError = subjectError,
                    supportingText = if (subjectError) { { Text("Subject is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Start Date *") },
                        readOnly = true,
                        isError = dateError,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                        enabled = false, // Use clickable on modifier instead
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Rounded.DateRange, contentDescription = "Select starting date")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        label = { Text("Due Date *") },
                        readOnly = true,
                        isError = dueDateError,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDueDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (dueDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = if (dueDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDueDatePicker = true }) {
                                Icon(Icons.Rounded.EventAvailable, contentDescription = "Select due date")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    titleError = title.isBlank()
                    subjectError = subject.isBlank()
                    dateError = date.isBlank()
                    dueDateError = dueDate.isBlank()

                    if (!titleError && !subjectError && !dateError && !dueDateError) {
                        onAddTask(title, description, subject, date, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UniPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.45f)
            ) {
                Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = AppSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onEditTask: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var subject by remember { mutableStateOf(task.subject) }
    var date by remember { mutableStateOf(task.date) }
    var dueDate by remember { mutableStateOf(task.dueDate) }

    // Error states
    var titleError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var dueDateError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var showDueDatePicker by remember { mutableStateOf(false) }
    val dueDatePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = convertMillisToDate(it)
                        dateError = false
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
                        dueDateError = false
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, tint = UniPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Edit Task")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; if (it.isNotBlank()) titleError = false },
                    label = { Text("Task Title *") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; if (it.isNotBlank()) subjectError = false },
                    label = { Text("Subject / Category *") },
                    isError = subjectError,
                    supportingText = if (subjectError) { { Text("Subject is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Start Date *") },
                        readOnly = true,
                        isError = dateError,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Rounded.DateRange, contentDescription = "Select starting date")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        label = { Text("Due Date *") },
                        readOnly = true,
                        isError = dueDateError,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDueDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (dueDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = if (dueDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDueDatePicker = true }) {
                                Icon(Icons.Rounded.EventAvailable, contentDescription = "Select due date")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    titleError = title.isBlank()
                    subjectError = subject.isBlank()
                    dateError = date.isBlank()
                    dueDateError = dueDate.isBlank()

                    if (!titleError && !subjectError && !dateError && !dueDateError) {
                        onEditTask(title, description, subject, date, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UniPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.45f)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = AppSurface
    )
}

// ─────────────────────────────────────────────
// HELPER FUNCTION FOR CATEGORY COLOR
// ─────────────────────────────────────────────
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "work" -> Color(0xFF4A90E2) // Blue
        "fun" -> Color(0xFFE67E22) // Orange
        "home" -> Color(0xFF27AE60) // Green
        "important" -> Color(0xFFE74C3C) // Red
        else -> AuroraSoftTeal
    }
}

// ─────────────────────────────────────────────
// TASK CARD (IMPROVED UI WITH CATEGORY CHIPS & BADGES)
// ─────────────────────────────────────────────
@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Determine category text and importance flag
    val categoryText = if (task.subject.isNotBlank()) task.subject else "General"
    val isImportant = task.subject.equals("Important", ignoreCase = true) ||
            task.title.contains("Important", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── HEADER SECTION ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category color indicator bar (subtle accent)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(getCategoryColor(categoryText))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title and metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getCategoryColor(categoryText).copy(alpha = 0.12f),
                            modifier = Modifier
                        ) {
                            Text(
                                text = categoryText,
                                style = MaterialTheme.typography.labelSmall,
                                color = getCategoryColor(categoryText),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Important Badge (if applicable)
                        if (isImportant) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE74C3C).copy(alpha = 0.12f),
                                modifier = Modifier
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PriorityHigh,
                                        contentDescription = "Important",
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFE74C3C)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Important",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE74C3C),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Options Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary
                        )
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
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = "Edit") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // ── BODY SECTION (Description) ─────────────────────────────────────
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── FOOTER SECTION (Dates) ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = "Start date",
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.date,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }

                // Due Date with divider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = "Due date",
                        modifier = Modifier.size(16.dp),
                        tint = if (isImportant) Color(0xFFE74C3C) else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isImportant) Color(0xFFE74C3C) else TextPrimary,
                        fontWeight = if (isImportant) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
