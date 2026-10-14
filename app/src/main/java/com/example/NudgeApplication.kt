package com.example

import android.app.Application
import com.example.data.local.NudgeDatabase
import com.example.data.repository.NudgeRepository
import com.example.notifications.NotificationHelper

class NudgeApplication : Application() {
  val database by lazy { NudgeDatabase.getDatabase(this) }
  val repository by lazy { NudgeRepository(this, database.nudgeDao()) }

  override fun onCreate() {
    super.onCreate()
    // Initialize Notification Channels
    NotificationHelper.createNotificationChannels(this)
  }
}
