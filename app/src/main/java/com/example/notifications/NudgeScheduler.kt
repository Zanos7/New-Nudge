package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Nudge
import com.example.data.model.ScheduleType
import com.example.receiver.NudgeAlarmReceiver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

object NudgeScheduler {
  const val ACTION_FIRE_NUDGE = "com.example.nudge.ACTION_FIRE_NUDGE"

  fun scheduleNextNudge(context: Context, nudge: Nudge) {
    if (!nudge.isEnabled) {
      cancelNudge(context, nudge.id)
      return
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerTimeMs = calculateNextTriggerTime(nudge)

    val intent = Intent(context, NudgeAlarmReceiver::class.java).apply {
      action = ACTION_FIRE_NUDGE
      putExtra(NotificationHelper.EXTRA_NUDGE_ID, nudge.id)
    }

    val requestCode = (nudge.id % 100000).toInt()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMs,
            pendingIntent
          )
        } else {
          alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMs,
            pendingIntent
          )
        }
      } else {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerTimeMs,
          pendingIntent
        )
      }
    } catch (_: SecurityException) {
      alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerTimeMs,
        pendingIntent
      )
    }
  }

  fun snoozeNudge(context: Context, nudgeId: Long, minutes: Int = 10) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerTimeMs = System.currentTimeMillis() + (minutes * 60 * 1000L)

    val intent = Intent(context, NudgeAlarmReceiver::class.java).apply {
      action = ACTION_FIRE_NUDGE
      putExtra(NotificationHelper.EXTRA_NUDGE_ID, nudgeId)
    }

    val requestCode = (nudgeId % 100000).toInt()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
      }
    } catch (_: Exception) {
      // Ignore
    }
  }

  fun cancelNudge(context: Context, nudgeId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, NudgeAlarmReceiver::class.java).apply {
      action = ACTION_FIRE_NUDGE
      putExtra(NotificationHelper.EXTRA_NUDGE_ID, nudgeId)
    }
    val requestCode = (nudgeId % 100000).toInt()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  private fun calculateNextTriggerTime(nudge: Nudge): Long {
    val now = LocalDateTime.now()
    val zoneId = ZoneId.systemDefault()
    val scheduleType = ScheduleType.fromName(nudge.scheduleType)
    val allowedDays = nudge.getDaysList().ifEmpty { listOf(1, 2, 3, 4, 5, 6, 7) }

    when (scheduleType) {
      ScheduleType.TIME_OF_DAY -> {
        val targetTime = LocalTime.of(nudge.hour.coerceIn(0, 23), nudge.minute.coerceIn(0, 59))
        for (dayOffset in 0..14) {
          val candidateDate = now.toLocalDate().plusDays(dayOffset.toLong())
          val dayOfWeekIso = candidateDate.dayOfWeek.value // 1=Mon .. 7=Sun
          if (dayOfWeekIso in allowedDays) {
            val candidateDateTime = LocalDateTime.of(candidateDate, targetTime)
            if (candidateDateTime.isAfter(now.plusSeconds(10))) {
              return candidateDateTime.atZone(zoneId).toInstant().toEpochMilli()
            }
          }
        }
        // Fallback: 24 hours later
        return now.plusDays(1).atZone(zoneId).toInstant().toEpochMilli()
      }

      ScheduleType.INTERVAL -> {
        val intervalMs = (nudge.intervalMinutes.coerceAtLeast(15)) * 60 * 1000L
        val currentHour = now.hour
        val startH = nudge.startHour.coerceIn(0, 23)
        val endH = nudge.endHour.coerceIn(0, 23)

        // Check if now is within active hours
        if (currentHour in startH until endH) {
          return System.currentTimeMillis() + intervalMs
        } else {
          // Jump to startHour tomorrow or next allowed day
          for (dayOffset in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(dayOffset.toLong())
            val dayOfWeekIso = candidateDate.dayOfWeek.value
            if (dayOfWeekIso in allowedDays) {
              val candidateDateTime = LocalDateTime.of(
                candidateDate,
                LocalTime.of(startH, 0)
              )
              if (candidateDateTime.isAfter(now.plusSeconds(10))) {
                return candidateDateTime.atZone(zoneId).toInstant().toEpochMilli()
              }
            }
          }
          return System.currentTimeMillis() + intervalMs
        }
      }

      ScheduleType.RANDOM_WINDOW -> {
        val startH = nudge.startHour.coerceIn(0, 22)
        val endH = nudge.endHour.coerceIn(startH + 1, 23)
        val randomHour = Random.nextInt(startH, endH + 1)
        val randomMinute = Random.nextInt(0, 60)
        val targetTime = LocalTime.of(randomHour, randomMinute)

        for (dayOffset in 0..7) {
          val candidateDate = now.toLocalDate().plusDays(dayOffset.toLong())
          val dayOfWeekIso = candidateDate.dayOfWeek.value
          if (dayOfWeekIso in allowedDays) {
            val candidateDateTime = LocalDateTime.of(candidateDate, targetTime)
            if (candidateDateTime.isAfter(now.plusSeconds(10))) {
              return candidateDateTime.atZone(zoneId).toInstant().toEpochMilli()
            }
          }
        }
        return System.currentTimeMillis() + (60 * 60 * 1000L)
      }
    }
  }
}
