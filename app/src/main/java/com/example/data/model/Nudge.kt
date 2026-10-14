package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class NudgeCategory(val displayName: String, val defaultIcon: String) {
  MINDFULNESS("Mindfulness", "SelfImprovement"),
  HYDRATION("Hydration", "WaterDrop"),
  POSTURE("Posture & Body", "AccessibilityNew"),
  FOCUS("Focus & Break", "Timer"),
  MOVEMENT("Movement", "DirectionsWalk"),
  CONNECTION("Connection", "Favorite"),
  HABIT("Daily Habit", "CheckCircle"),
  CUSTOM("Custom", "NotificationsActive");

  companion object {
    fun fromName(name: String?): NudgeCategory {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: MINDFULNESS
    }
  }
}

enum class ScheduleType(val displayName: String) {
  TIME_OF_DAY("Specific Time"),
  INTERVAL("Repeating Interval"),
  RANDOM_WINDOW("Random Gentle");

  companion object {
    fun fromName(name: String?): ScheduleType {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: TIME_OF_DAY
    }
  }
}

enum class NudgePriority(val displayName: String) {
  GENTLE("Gentle"),
  NORMAL("Standard"),
  URGENT("Priority");

  companion object {
    fun fromName(name: String?): NudgePriority {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: NORMAL
    }
  }
}

@Entity(tableName = "nudges")
data class Nudge(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val message: String,
  val category: String = NudgeCategory.MINDFULNESS.name,
  val scheduleType: String = ScheduleType.TIME_OF_DAY.name,
  val hour: Int = 9,
  val minute: Int = 0,
  val intervalMinutes: Int = 60,
  val startHour: Int = 9,
  val endHour: Int = 18,
  val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
  val isEnabled: Boolean = true,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val lastCompletedEpochDay: Long = -1L,
  val priority: String = NudgePriority.NORMAL.name,
  val soundEnabled: Boolean = true,
  val vibrateEnabled: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
) {
  val isCompletedToday: Boolean
    get() {
      val todayEpochDay = LocalDate.now().toEpochDay()
      return lastCompletedEpochDay == todayEpochDay
    }

  fun getDaysList(): List<Int> {
    return daysOfWeek.split(",")
      .mapNotNull { it.trim().toIntOrNull() }
      .filter { it in 1..7 }
  }

  fun formatTime(): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val m = String.format("%02d", minute)
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h:$m $amPm"
  }

  fun formatScheduleSummary(): String {
    val sched = ScheduleType.fromName(scheduleType)
    return when (sched) {
      ScheduleType.TIME_OF_DAY -> "Daily at ${formatTime()}"
      ScheduleType.INTERVAL -> "Every ${intervalMinutes}m between ${formatHour(startHour)} - ${formatHour(endHour)}"
      ScheduleType.RANDOM_WINDOW -> "Random nudge between ${formatHour(startHour)} - ${formatHour(endHour)}"
    }
  }

  private fun formatHour(h: Int): String {
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    val amPm = if (h < 12) "AM" else "PM"
    return "$displayH $amPm"
  }
}
