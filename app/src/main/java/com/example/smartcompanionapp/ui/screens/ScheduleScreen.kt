package com.example.smartcompanionapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
 * so tasks added there appear here automatically.
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
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy") // Using full month name for better UI

    // Currently selected day number (1–31)
    var selectedDay by remember { mutableStateOf(1) }

    // Calculate the day of the week the 1st of the month falls on to correctly align the calendar grid
    // DayOfWeek ranges from 1 (Monday) to 7 (Sunday). We use modulo 7 to map Sunday to 0.
    val firstDayOfWeekOffset = remember(currentMonth) {
        currentMonth.atDay(1).dayOfWeek.value % 7
    }

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
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar grid — shows headers, offset empty days, and the correct number of days
            CalendarGrid(
                daysInMonth = currentMonth.lengthOfMonth(),
                emptyDaysOffset = firstDayOfWeekOffset,
                selectedDay = selectedDay.toString(),
                onDaySelected = { selectedDay = it.toInt() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header
            Text(
                text = "Tasks for $selectedDateString",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── STATE DRIVEN LIST RENDERING ───────────────────────────────────
            // Reads from the shared UiState and filters tasks by the selected date.
            // If the state is Success, filter tasks whose `date` matches selectedDateString.
            // This re-runs automatically whenever selectedDay, currentMonth, or the task list changes.
            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UniPrimary)
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
                        Text(state.message, color = MaterialTheme.colorScheme.error)
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
        title = {
            Text(
                "Schedule",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppBackground,
            titleContentColor = TextPrimary
        )
    )
}

// ─────────────────────────────────────────────
// MONTH NAVIGATION
// ─────────────────────────────────────────────
// Styled row with prev/next icon buttons and an emphasized current month label.
@Composable
fun MonthNavigation(currentMonth: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .background(AppSurface, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous Month", tint = TextPrimary)
        }

        Text(
            text = currentMonth,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .background(AppSurface, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next Month", tint = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────
// CALENDAR GRID
// ─────────────────────────────────────────────
// Renders day-of-week headers and a 7-column grid with the correct number of days for the month.
// Includes empty offset blocks so the 1st of the month starts on the correct weekday.
@Composable
fun CalendarGrid(
    daysInMonth: Int,
    emptyDaysOffset: Int,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Day of Week Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Dates
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between columns
        ) {
            // Invisible offset cells for the start of the month
            items(emptyDaysOffset) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Actual interactable days
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
}

// ─────────────────────────────────────────────
// CALENDAR DAY
// ─────────────────────────────────────────────
// Individual circle cell. Animated selection highlight in UniPrimary when selected.
@Composable
fun CalendarDay(day: String, isSelected: Boolean, onClick: () -> Unit) {
    // Smooth transition between selected and unselected states
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) UniPrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "dayBackgroundColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "dayTextColor"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Ensure it's perfectly square/circular
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────
// TASK LIST
// ─────────────────────────────────────────────
// Scrollable list of ScheduleCards for the filtered tasks.
@Composable
fun TaskList(tasks: List<Task>, selectedDateString: String) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            ScheduleCard(task, selectedDateString)
        }
    }
}

// ─────────────────────────────────────────────
// SCHEDULE CARD
// ─────────────────────────────────────────────
// Visually elevated card showing a single task's details.
@Composable
fun ScheduleCard(task: Task, selectedDateString: String) {
    val isStartingDate = task.date == selectedDateString

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Type/Status Indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(UniPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStartingDate) Icons.Rounded.Event else Icons.Rounded.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = UniPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.subject,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timeline / Due Date tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isStartingDate) "Starts: ${task.date}" else "Due: ${task.dueDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// EMPTY STATE PLACEHOLDER
// ─────────────────────────────────────────────
// Beautiful empty state shown when no tasks are scheduled for the selected day.
@Composable
fun NoTasksPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.EventBusy,
            contentDescription = "No tasks",
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tasks for this day",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No pending tasks for this day!",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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