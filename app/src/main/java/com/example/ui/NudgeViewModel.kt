package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NudgeApplication
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.data.model.NudgeLog
import com.example.data.model.NudgePriority
import com.example.data.model.ScheduleType
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayProgress(
  val totalActive: Int = 0,
  val completedCount: Int = 0,
  val percent: Float = 0f
)

class NudgeViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = (application as NudgeApplication).repository

  val allNudges: StateFlow<List<Nudge>> = repository.allNudges.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val recentLogs: StateFlow<List<NudgeLog>> = repository.recentLogs.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
  val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

  val filteredNudges: StateFlow<List<Nudge>> = combine(allNudges, _selectedCategoryFilter) { list, filter ->
    if (filter == null) list else list.filter { it.category.equals(filter, ignoreCase = true) }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayNudges: StateFlow<List<Nudge>> = allNudges.combine(MutableStateFlow(LocalDate.now())) { list, today ->
    val dayOfWeek = today.dayOfWeek.value
    list.filter { nudge ->
      nudge.isEnabled && nudge.getDaysList().contains(dayOfWeek)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayProgress: StateFlow<TodayProgress> = todayNudges.combine(MutableStateFlow(Unit)) { list, _ ->
    val total = list.size
    val completed = list.count { it.isCompletedToday }
    val pct = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    TodayProgress(totalActive = total, completedCount = completed, percent = pct)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayProgress())

  val maxStreak: StateFlow<Int> = allNudges.combine(MutableStateFlow(Unit)) { list, _ ->
    list.maxOfOrNull { it.currentStreak } ?: 0
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _isEditorOpen = MutableStateFlow(false)
  val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

  private val _editingNudge = MutableStateFlow<Nudge?>(null)
  val editingNudge: StateFlow<Nudge?> = _editingNudge.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  private val _permissionGranted = MutableStateFlow(false)
  val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

  fun setCategoryFilter(category: String?) {
    _selectedCategoryFilter.value = category
  }

  fun setPermissionGranted(granted: Boolean) {
    _permissionGranted.value = granted
  }

  fun openCreateEditor() {
    _editingNudge.value = null
    _isEditorOpen.value = true
  }

  fun openEditEditor(nudge: Nudge) {
    _editingNudge.value = nudge
    _isEditorOpen.value = true
  }

  fun closeEditor() {
    _isEditorOpen.value = false
    _editingNudge.value = null
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun toggleNudge(nudgeId: Long, isEnabled: Boolean) {
    viewModelScope.launch {
      repository.toggleNudge(nudgeId, isEnabled)
      _userMessage.value = if (isEnabled) "Nudge activated" else "Nudge paused"
    }
  }

  fun completeNudge(nudge: Nudge) {
    viewModelScope.launch {
      repository.completeNudge(nudge)
      _userMessage.value = "Great job! ${nudge.title} completed 🎉"
    }
  }

  fun deleteNudge(nudge: Nudge) {
    viewModelScope.launch {
      repository.deleteNudge(nudge)
      _userMessage.value = "Nudge removed"
    }
  }

  fun saveNudge(
    id: Long,
    title: String,
    message: String,
    category: NudgeCategory,
    scheduleType: ScheduleType,
    hour: Int,
    minute: Int,
    intervalMinutes: Int,
    startHour: Int,
    endHour: Int,
    daysOfWeek: String,
    priority: NudgePriority,
    soundEnabled: Boolean,
    vibrateEnabled: Boolean
  ) {
    viewModelScope.launch {
      val existing = if (id > 0) repository.getNudge(id) else null
      val nudge = Nudge(
        id = id,
        title = title.trim(),
        message = message.trim(),
        category = category.name,
        scheduleType = scheduleType.name,
        hour = hour,
        minute = minute,
        intervalMinutes = intervalMinutes,
        startHour = startHour,
        endHour = endHour,
        daysOfWeek = daysOfWeek,
        isEnabled = true,
        currentStreak = existing?.currentStreak ?: 0,
        bestStreak = existing?.bestStreak ?: 0,
        lastCompletedEpochDay = existing?.lastCompletedEpochDay ?: -1L,
        priority = priority.name,
        soundEnabled = soundEnabled,
        vibrateEnabled = vibrateEnabled
      )

      if (id > 0) {
        repository.updateNudge(nudge)
        _userMessage.value = "Nudge updated"
      } else {
        repository.addNudge(nudge)
        _userMessage.value = "New nudge scheduled"
      }
      closeEditor()
    }
  }

  fun triggerPushNudge(nudge: Nudge) {
    viewModelScope.launch {
      NotificationHelper.showNudgeNotification(getApplication(), nudge)
      repository.logManualTrigger(nudge)
      _userMessage.value = "Push notification sent for \"${nudge.title}\""
    }
  }

  fun triggerInstantTestPush(
    title: String = "Hydrate & Refresh",
    message: String = "Take a refreshing sip of water and pause for a quiet breath.",
    category: String = NudgeCategory.HYDRATION.name,
    priority: String = NudgePriority.NORMAL.name
  ) {
    viewModelScope.launch {
      NotificationHelper.showNotification(
        context = getApplication(),
        nudgeId = System.currentTimeMillis() % 10000,
        title = title,
        message = message,
        category = category,
        priority = priority
      )
      _userMessage.value = "Test push notification dispatched! Check your status bar."
    }
  }
}
