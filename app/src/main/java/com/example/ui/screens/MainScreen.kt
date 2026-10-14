package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NudgeViewModel
import com.example.ui.dialogs.NudgeEditorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NudgeViewModel) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  val allNudges by viewModel.allNudges.collectAsStateWithLifecycle()
  val filteredNudges by viewModel.filteredNudges.collectAsStateWithLifecycle()
  val todayNudges by viewModel.todayNudges.collectAsStateWithLifecycle()
  val todayProgress by viewModel.todayProgress.collectAsStateWithLifecycle()
  val maxStreak by viewModel.maxStreak.collectAsStateWithLifecycle()
  val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val isEditorOpen by viewModel.isEditorOpen.collectAsStateWithLifecycle()
  val editingNudge by viewModel.editingNudge.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
  val isPermissionGranted by viewModel.permissionGranted.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) }

  // Check initial notification permission
  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val isGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      viewModel.setPermissionGranted(isGranted)
    } else {
      viewModel.setPermissionGranted(true)
    }
  }

  // Permission Launcher for Android 13+ (POST_NOTIFICATIONS)
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    viewModel.setPermissionGranted(granted)
    if (granted) {
      viewModel.triggerInstantTestPush(
        title = "Push Notifications Enabled!",
        message = "You will now receive gentle nudges and timely check-ins."
      )
    }
  }

  fun requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Handle snackbar messages
  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Nudge",
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
        },
        actions = {
          // Quick status indicator
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Push Notifications",
            tint = if (isPermissionGranted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 16.dp)
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Filled.DateRange else Icons.Outlined.DateRange,
              contentDescription = "Today"
            )
          },
          label = { Text("Today") },
          modifier = Modifier.testTag("tab_today")
        )
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 1) Icons.Filled.FormatListBulleted else Icons.Outlined.FormatListBulleted,
              contentDescription = "All Nudges"
            )
          },
          label = { Text("Nudges") },
          modifier = Modifier.testTag("tab_nudges")
        )
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Filled.Insights else Icons.Outlined.Insights,
              contentDescription = "Insights & Notifications"
            )
          },
          label = { Text("Push & Stats") },
          modifier = Modifier.testTag("tab_stats")
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
        when (tab) {
          0 -> TodayScreen(
            todayNudges = todayNudges,
            progress = todayProgress,
            maxStreak = maxStreak,
            isPermissionGranted = isPermissionGranted,
            onRequestPermission = ::requestPermission,
            onCompleteNudge = { viewModel.completeNudge(it) },
            onTriggerPush = { viewModel.triggerPushNudge(it) },
            onInstantTestPush = { viewModel.triggerInstantTestPush() },
            onAddNewNudge = { viewModel.openCreateEditor() }
          )

          1 -> AllNudgesScreen(
            nudges = filteredNudges,
            selectedCategory = selectedCategory,
            onSelectCategory = { viewModel.setCategoryFilter(it) },
            onToggleNudge = { id, enabled -> viewModel.toggleNudge(id, enabled) },
            onEditNudge = { viewModel.openEditEditor(it) },
            onDeleteNudge = { viewModel.deleteNudge(it) },
            onTriggerPush = { viewModel.triggerPushNudge(it) },
            onAddNewNudge = { viewModel.openCreateEditor() }
          )

          2 -> StatsAndSettingsScreen(
            nudges = allNudges,
            recentLogs = recentLogs,
            maxStreak = maxStreak,
            isPermissionGranted = isPermissionGranted,
            onRequestPermission = ::requestPermission,
            onTestPush = { title, msg, cat, prio ->
              viewModel.triggerInstantTestPush(
                title = title,
                message = msg,
                category = cat,
                priority = prio
              )
            }
          )
        }
      }

      // Nudge Editor Modal Bottom Sheet
      if (isEditorOpen) {
        NudgeEditorDialog(
          nudge = editingNudge,
          onDismiss = { viewModel.closeEditor() },
          onSave = { id, title, msg, cat, sched, hr, min, interval, startHr, endHr, days, prio, snd, vib ->
            viewModel.saveNudge(
              id = id,
              title = title,
              message = msg,
              category = cat,
              scheduleType = sched,
              hour = hr,
              minute = min,
              intervalMinutes = interval,
              startHour = startHr,
              endHour = endHr,
              daysOfWeek = days,
              priority = prio,
              soundEnabled = snd,
              vibrateEnabled = vib
            )
          }
        )
      }
    }
  }
}
