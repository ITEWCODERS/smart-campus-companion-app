package com.example.smartcompanionapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.domain.TaskUiState
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// MAIN SCHEDULE SCREEN
// ─────────────────────────────────────────────
/**
 * Displays a monthly calendar. When the user taps a day, it filters and shows
 * only the tasks whose `date` field (scheduled date) matches the selected day
 * in the current month (e.g. "15/01/2026").
 *
 * @param navController Controller for navigation between screens.
 * @param viewModel     Shared TaskViewModel — same instance used by TaskScreen,
 *                      so tasks added there appear here automatically.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: TaskViewModel  // Receives the shared ViewModel to read tasks from
) {
    // ── STATE DRIVEN LIST RENDERING ──────────────────────────────────────────
    // Observes the same StateFlow used by TaskScreen.
    // Whenever a task is added/edited/deleted in TaskScreen, this screen
    // automatically recomposes with the updated list — no manual refresh needed.
    val uiState by viewModel.uiState.collectAsState()

    // Current visible month (e.g. March 2026)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    // Currently selected day number (1–31)
    var selectedDay by remember { mutableStateOf(1) }

    // Build the full "dd/MM/yyyy" string for the selected day so we can filter tasks
    // e.g. day=5, month=March 2026 → "05/03/2026"
    val selectedDateString = remember(selectedDay, currentMonth) {
        String.format("%02d/%02d/%04d", selectedDay, currentMonth.monthValue, currentMonth.year)
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = { ScheduleTopAppBar(navController) },
        bottomBar = { BottomNavWithController(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            MonthNavigation(
                currentMonth = currentMonth.format(monthFormatter),
                onPrevious = {
                    currentMonth = currentMonth.minusMonths(1)
                    selectedDay = 1 // Reset to day 1 when changing months
                },
                onNext = {
                    currentMonth = currentMonth.plusMonths(1)
                    selectedDay = 1
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid — shows the correct number of days for the current month
            CalendarGrid(
                daysInMonth = currentMonth.lengthOfMonth(),
                selectedDay = selectedDay.toString(),
                onDaySelected = { selectedDay = it.toInt() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── STATE DRIVEN LIST RENDERING ───────────────────────────────────
            // Reads from the shared UiState and filters tasks by the selected date.
            // If the state is Success, filter tasks whose `date` matches selectedDateString.
            // This re-runs automatically whenever selectedDay, currentMonth, or the task list changes.
            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is TaskUiState.Success -> {
                    // Filter tasks: show tasks scheduled for the selected day OR due on the selected day
                    // task.date and task.dueDate are stored as "dd/MM/yyyy"
                    val filteredTasks = state.tasks.filter { it.date == selectedDateString || it.dueDate == selectedDateString }

                    if (filteredTasks.isEmpty()) {
                        NoTasksPlaceholder()
                    } else {
                        TaskList(tasks = filteredTasks, selectedDateString = selectedDateString)
                    }
                }

                is TaskUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTopAppBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Schedule") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

// ─────────────────────────────────────────────
// MONTH NAVIGATION
// ─────────────────────────────────────────────
// Simple row with prev/next buttons and the current month label.
@Composable
fun MonthNavigation(currentMonth: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("<", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onPrevious() })
        Text(currentMonth, fontWeight = FontWeight.Bold)
        Text(">", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNext() })
    }
}

// ─────────────────────────────────────────────
// CALENDAR GRID
// ─────────────────────────────────────────────
// Renders a 7-column grid with the correct number of days for the month.
// Highlights the currently selected day.
@Composable
fun CalendarGrid(
    daysInMonth: Int,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(daysInMonth) { index ->
            val day = (index + 1).toString()
            CalendarDay(
                day = day,
                isSelected = selectedDay == day,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

// ─────────────────────────────────────────────
// CALENDAR DAY
// ─────────────────────────────────────────────
// Individual circle cell. Highlighted in UniPrimary when selected.
@Composable
fun CalendarDay(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isSelected) UniPrimary else AppSurface,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────
// TASK LIST
// ─────────────────────────────────────────────
// Scrollable list of ScheduleCards for the filtered tasks.
// Used by ScheduleScreen after filtering state.tasks by the selected date.
@Composable
fun TaskList(tasks: List<Task>, selectedDateString: String) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            ScheduleCard(task, selectedDateString)
        }
    }
}

// ─────────────────────────────────────────────
// SCHEDULE CARD
// ─────────────────────────────────────────────
// Card showing a single task's title and date/due date in the schedule view.
@Composable
fun ScheduleCard(task: Task, selectedDateString: String) {
    val isStartingDate = task.date == selectedDateString
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(UniPrimary, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(task.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Subject: ${task.subject}", fontSize = 12.sp, color = TextSecondary)
                if (isStartingDate) {
                    Text("Starting at: ${task.date}", fontSize = 12.sp, color = TextSecondary)
                } else {
                    Text("Due: ${task.dueDate}", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// EMPTY STATE PLACEHOLDER
// ─────────────────────────────────────────────
// Shown when no tasks are scheduled for the selected day.
@Composable
fun NoTasksPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No tasks scheduled", color = TextSecondary)
    }
}

// ─────────────────────────────────────────────
// BOTTOM NAVIGATION BAR
// ─────────────────────────────────────────────
@Composable
fun BottomNavWithController(navController: NavController) {
    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("dashboard") },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("schedule") },
            icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("task") },
            icon = { Icon(Icons.Rounded.Checklist, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("campusInfo") },
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Campus Info") },
            label = { Text("Info") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("settings") },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
