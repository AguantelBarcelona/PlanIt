package com.tuempresa.PlanIt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.tuempresa.PlanIt.domain.models.Task
import com.tuempresa.PlanIt.navigation.Routes
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, viewModel: TaskViewModel, userId: Int) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    LaunchedEffect(userId) {
        viewModel.loadTasks(userId)
    }

    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusMonths(100)
    val endMonth = currentMonth.plusMonths(100)
    val firstDayOfWeek = firstDayOfWeekFromLocale()

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tareas") },
                    label = { Text("Tareas") },
                    selected = false,
                    onClick = { navController.navigate(Routes.TaskList.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    selected = true,
                    onClick = { /* Ya estamos aquí */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate(Routes.Profile.route) }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    scope.launch {
                        val newMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                        calendarState.animateScrollToMonth(newMonth)
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
                }
                Text(
                    text = "${calendarState.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendarState.firstVisibleMonth.yearMonth.year}",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = {
                    scope.launch {
                        val newMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                        calendarState.animateScrollToMonth(newMonth)
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente")
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            val tasks = if (uiState is TaskViewModel.TaskUiState.Success) (uiState as TaskViewModel.TaskUiState.Success).tasks else emptyList()
            HorizontalCalendar(
                state = calendarState,
                dayContent = { day ->
                    Day(
                        day = day,
                        tasks = tasks.filter { it.dueDate != null && it.dueDate / 1000 / 60 / 60 / 24 == day.date.toEpochDay() },
                        isSelected = selectedDate == day.date,
                        onClick = { selectedDate = it.date }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val tasksForSelectedDay = selectedDate?.let {
                    date -> tasks.filter { it.dueDate != null && it.dueDate / 1000 / 60 / 60 / 24 == date.toEpochDay() }
                } ?: emptyList()

                if (tasksForSelectedDay.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    ) {
                        items(tasksForSelectedDay) { task ->
                            val isPastDueDate = task.dueDate?.let { it < System.currentTimeMillis() - 86400000 } ?: false
                            val isClickable = !task.isCompleted && !isPastDueDate
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = isClickable) { 
                                        navController.navigate(Routes.TaskEdit.route + "?taskId=${task.id}") 
                                    },
                            ) {
                                Text(
                                    text = task.title,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (selectedDate != null) "No hay tareas para este día." else "Selecciona un día para ver las tareas.")
                    }
                }
            }
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    tasks: List<Task>,
    isSelected: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        if (day.position == DayPosition.MonthDate) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Unspecified
                )
                Row {
                    tasks.take(4).forEach { task ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(horizontal = 1.dp)
                                .background(
                                    if (task.isCompleted) Color.Green else Color.Red,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
