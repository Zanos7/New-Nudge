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

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
      intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
    ) {
      NotificationHelper.createNotificationChannels(context)

      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val database = NudgeDatabase.getDatabase(context)
          val activeNudges = database.nudgeDao().getActiveNudgesSync()
          for (nudge in activeNudges) {
            NudgeScheduler.scheduleNextNudge(context, nudge)
          }
        } finally {
          pendingResult.finish()
        }
      }
    }
  }
}
