package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ScheduleItem
import com.example.notification.NotificationHelper
import com.example.ui.components.AddEditScheduleDialog
import com.example.ui.components.CircularTimetable
import com.example.ui.components.DateHeader
import com.example.ui.components.ScheduleListView
import com.example.ui.components.StatsSummaryView
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ScheduleViewModel
import com.example.viewmodel.ViewMode

class MainActivity : ComponentActivity() {
    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ScheduleViewModel) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isToday = viewModel.isTodaySelected()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val currentMinute by viewModel.currentMinute.collectAsStateWithLifecycle()
    val allSchedules by viewModel.schedulesForSelectedDate.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ScheduleItem?>(null) }
    var addInitialAllDay by remember { mutableStateOf(false) }

    // Request Notification Permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_screen_scaffold"),
        topBar = {
            DateHeader(
                selectedDate = selectedDate,
                isToday = isToday,
                dailyStats = dailyStats,
                viewMode = viewMode,
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() },
                onGoToToday = { viewModel.goToToday() },
                onDateSelected = { viewModel.selectDate(it) },
                onViewModeChange = { viewModel.setViewMode(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    addInitialAllDay = false
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_schedule")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새 일정/할일 추가",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "view_mode_anim"
            ) { targetMode ->
                when (targetMode) {
                    ViewMode.CIRCULAR -> {
                        CircularTimetable(
                            schedules = allSchedules,
                            selectedItem = selectedItem,
                            currentMinute = currentMinute,
                            isToday = isToday,
                            dailyStats = dailyStats,
                            onSelectItem = { viewModel.selectItem(it) },
                            onToggleComplete = { viewModel.toggleComplete(it) },
                            onEditItem = {
                                itemToEdit = it
                                showAddDialog = true
                            },
                            onDeleteItem = { viewModel.deleteSchedule(it) },
                            onAddNewItem = {
                                itemToEdit = null
                                addInitialAllDay = true
                                showAddDialog = true
                            }
                        )
                    }

                    ViewMode.LIST -> {
                        ScheduleListView(
                            items = filteredItems,
                            filterStatus = filterStatus,
                            selectedCategory = selectedCategory,
                            onFilterStatusChange = { viewModel.setFilterStatus(it) },
                            onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                            onToggleComplete = { viewModel.toggleComplete(it) },
                            onEditItem = {
                                itemToEdit = it
                                showAddDialog = true
                            },
                            onDeleteItem = { viewModel.deleteSchedule(it) },
                            onAddNewItem = {
                                itemToEdit = null
                                addInitialAllDay = false
                                showAddDialog = true
                            }
                        )
                    }

                    ViewMode.STATS -> {
                        StatsSummaryView(
                            items = allSchedules,
                            dailyStats = dailyStats
                        )
                    }
                }
            }

            // Add / Edit Dialog
            if (showAddDialog) {
                AddEditScheduleDialog(
                    itemToEdit = itemToEdit,
                    initialIsAllDay = addInitialAllDay,
                    onDismiss = {
                        showAddDialog = false
                        itemToEdit = null
                    },
                    onSave = { savedItem ->
                        viewModel.saveSchedule(savedItem)
                        showAddDialog = false
                        itemToEdit = null
                    },
                    onDelete = { itemToDelete ->
                        viewModel.deleteSchedule(itemToDelete)
                        showAddDialog = false
                        itemToEdit = null
                    }
                )
            }
        }
    }
}
