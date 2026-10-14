package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.data.model.NudgePriority
import com.example.receiver.NudgeActionReceiver

object NotificationHelper {
  const val CHANNEL_GENTLE_ID = "nudge_channel_gentle"
  const val CHANNEL_NORMAL_ID = "nudge_channel_normal"
  const val CHANNEL_URGENT_ID = "nudge_channel_urgent"

  const val EXTRA_NUDGE_ID = "extra_nudge_id"
  const val EXTRA_NUDGE_TITLE = "extra_nudge_title"
  const val EXTRA_NUDGE_CATEGORY = "extra_nudge_category"
  const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

  const val ACTION_DONE = "com.example.nudge.ACTION_DONE"
  const val ACTION_SNOOZE = "com.example.nudge.ACTION_SNOOZE"

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // Gentle Channel: Soft attention, low vibration
      val gentleChannel = NotificationChannel(
        CHANNEL_GENTLE_ID,
        context.getString(R.string.channel_gentle_name),
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = context.getString(R.string.channel_gentle_desc)
        enableLights(true)
        lightColor = Color.parseColor("#80CBC4")
        enableVibration(false)
        setShowBadge(true)
      }

      // Normal Channel: Standard habit reminder
      val normalChannel = NotificationChannel(
        CHANNEL_NORMAL_ID,
        context.getString(R.string.channel_normal_name),
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = context.getString(R.string.channel_normal_desc)
        enableLights(true)
        lightColor = Color.parseColor("#00695C")
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 200, 100, 200)
        setShowBadge(true)
      }

      // Urgent Channel: High priority
      val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()

      val urgentChannel = NotificationChannel(
        CHANNEL_URGENT_ID,
        context.getString(R.string.channel_urgent_name),
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = context.getString(R.string.channel_urgent_desc)
        enableLights(true)
        lightColor = Color.parseColor("#BF360C")
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 300, 150, 300)
        setSound(soundUri, audioAttributes)
        setShowBadge(true)
      }

      notificationManager.createNotificationChannels(
        listOf(gentleChannel, normalChannel, urgentChannel)
      )
    }
  }

  fun showNudgeNotification(context: Context, nudge: Nudge) {
    showNotification(
      context = context,
      nudgeId = nudge.id,
      title = nudge.title,
      message = nudge.message,
      category = nudge.category,
      priority = nudge.priority,
      soundEnabled = nudge.soundEnabled,
      vibrateEnabled = nudge.vibrateEnabled
    )
  }

  fun showNotification(
    context: Context,
    nudgeId: Long,
    title: String,
    message: String,
    category: String,
    priority: String,
    soundEnabled: Boolean = true,
    vibrateEnabled: Boolean = true
  ) {
    createNotificationChannels(context)

    val notificationId = (nudgeId % 100000).toInt().coerceAtLeast(1)

    // Tap action: open MainActivity
    val contentIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_NUDGE_ID, nudgeId)
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      contentIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action button: Done
    val doneIntent = Intent(context, NudgeActionReceiver::class.java).apply {
      action = ACTION_DONE
      putExtra(EXTRA_NUDGE_ID, nudgeId)
      putExtra(EXTRA_NUDGE_TITLE, title)
      putExtra(EXTRA_NUDGE_CATEGORY, category)
      putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }
    val donePendingIntent = PendingIntent.getBroadcast(
      context,
      notificationId * 10 + 1,
      doneIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action button: Snooze
    val snoozeIntent = Intent(context, NudgeActionReceiver::class.java).apply {
      action = ACTION_SNOOZE
      putExtra(EXTRA_NUDGE_ID, nudgeId)
      putExtra(EXTRA_NUDGE_TITLE, title)
      putExtra(EXTRA_NUDGE_CATEGORY, category)
      putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }
    val snoozePendingIntent = PendingIntent.getBroadcast(
      context,
      notificationId * 10 + 2,
      snoozeIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val channelId = when (NudgePriority.fromName(priority)) {
      NudgePriority.GENTLE -> CHANNEL_GENTLE_ID
      NudgePriority.NORMAL -> CHANNEL_NORMAL_ID
      NudgePriority.URGENT -> CHANNEL_URGENT_ID
    }

    val categoryEnum = NudgeCategory.fromName(category)
    val subText = "Nudge • ${categoryEnum.displayName}"

    val builder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_notification_nudge)
      .setContentTitle(title)
      .setContentText(message)
      .setSubText(subText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setColor(0xFF00695C.toInt())
      .setContentIntent(contentPendingIntent)
      .setAutoCancel(true)
      .setPriority(
        when (NudgePriority.fromName(priority)) {
          NudgePriority.GENTLE -> NotificationCompat.PRIORITY_LOW
          NudgePriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
          NudgePriority.URGENT -> NotificationCompat.PRIORITY_HIGH
        }
      )
      .addAction(R.drawable.ic_notification_nudge, "✓ Done", donePendingIntent)
      .addAction(R.drawable.ic_notification_nudge, "⏰ Snooze 10m", snoozePendingIntent)

    if (!soundEnabled) {
      builder.setSilent(true)
    }
    if (!vibrateEnabled) {
      builder.setVibrate(longArrayOf(0))
    }

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (_: SecurityException) {
      // Handled if permission was revoked
    }
  }

  fun cancelNotification(context: Context, nudgeId: Long) {
    val notificationId = (nudgeId % 100000).toInt().coerceAtLeast(1)
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
  }
}
