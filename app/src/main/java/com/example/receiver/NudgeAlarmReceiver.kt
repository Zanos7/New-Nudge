package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.NudgeDatabase
import com.example.notifications.NotificationHelper
import com.example.notifications.NudgeScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NudgeAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val nudgeId = intent?.getLongExtra(NotificationHelper.EXTRA_NUDGE_ID, -1L) ?: return
    if (nudgeId <= 0L) return

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val database = NudgeDatabase.getDatabase(context)
        val nudge = database.nudgeDao().getNudgeById(nudgeId)
        if (nudge != null && nudge.isEnabled) {
          NotificationHelper.showNudgeNotification(context, nudge)
          NudgeScheduler.scheduleNextNudge(context, nudge)
        }
      } finally {
        pendingResult.finish()
      }
    }
  }
}
