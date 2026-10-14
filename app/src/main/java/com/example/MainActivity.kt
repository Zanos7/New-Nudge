package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.notifications.NotificationHelper
import com.example.ui.NudgeViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.NudgeTheme

class MainActivity : ComponentActivity() {
  private val viewModel: NudgeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Ensure channels are active
    NotificationHelper.createNotificationChannels(this)

    setContent {
      NudgeTheme {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}
