package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.NudgeDatabase
import com.example.data.model.NudgeLog
import com.example.notifications.NotificationHelper
import com.example.notifications.NudgeScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NudgeActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    val nudgeId = intent.getLongExtra(NotificationHelper.EXTRA_NUDGE_ID, -1L)
    val title = intent.getStringExtra(NotificationHelper.EXTRA_NUDGE_TITLE) ?: "Nudge"
    val category = intent.getStringExtra(NotificationHelper.EXTRA_NUDGE_CATEGORY) ?: "Mindfulness"

    // Always dismiss notification
    NotificationHelper.cancelNotification(context, nudgeId)

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val database = NudgeDatabase.getDatabase(context)
        val dao = database.nudgeDao()

        when (action) {
          NotificationHelper.ACTION_DONE -> {
            val nudge = dao.getNudgeById(nudgeId)
            val todayEpochDay = LocalDate.now().toEpochDay()
            if (nudge != null) {
              val isConsecutive = nudge.lastCompletedEpochDay == todayEpochDay - 1
              val newStreak = if (nudge.lastCompletedEpochDay == todayEpochDay) {
                nudge.currentStreak
              } else if (isConsecutive) {
                nudge.currentStreak + 1
              } else {
                1
              }
              val newBestStreak = maxOf(nudge.bestStreak, newStreak)

              dao.updateCompletion(nudge.id, newStreak, newBestStreak, todayEpochDay)
              dao.insertLog(
                NudgeLog(
                  nudgeId = nudge.id,
                  nudgeTitle = nudge.title,
                  category = nudge.category,
                  action = "COMPLETED"
                )
              )
              NudgeScheduler.scheduleNextNudge(context, nudge)
            }
          }

          NotificationHelper.ACTION_SNOOZE -> {
            dao.insertLog(
              NudgeLog(
                nudgeId = nudgeId,
                nudgeTitle = title,
                category = category,
                action = "SNOOZED"
              )
            )
            // Snooze for 10 minutes
            NudgeScheduler.snoozeNudge(context, nudgeId, 10)
          }
        }
      } finally {
        pendingResult.finish()
      }
    }
  }
}
