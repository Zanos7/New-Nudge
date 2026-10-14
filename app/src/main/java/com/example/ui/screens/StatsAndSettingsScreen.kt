package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.data.model.NudgeLog
import com.example.data.model.NudgePriority
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsAndSettingsScreen(
  nudges: List<Nudge>,
  recentLogs: List<NudgeLog>,
  maxStreak: Int,
  isPermissionGranted: Boolean,
  onRequestPermission: () -> Unit,
  onTestPush: (title: String, message: String, category: String, priority: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val totalCompletions = recentLogs.count { it.action == "COMPLETED" }
  val activeNudgesCount = nudges.count { it.isEnabled }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
          text = "Notifications & Insights",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Push notification engine and habit statistics",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Stats Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Active Nudges
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Active Nudges",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$activeNudgesCount",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Longest Streak
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Best Streak",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$maxStreak days",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.tertiary
            )
          }
        }
      }
    }

    // Push Notification System Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Push Notification Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isPermissionGranted) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.errorContainer
            ) {
              Text(
                text = if (isPermissionGranted) "Active" else "Permission Required",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPermissionGranted) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Nudge uses native Android notification channels and alarm triggers. Tap below to send instant push notifications to your notification shade to test them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (!isPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onRequestPermission,
              modifier = Modifier.fillMaxWidth().testTag("grant_notification_settings_button")
            ) {
              Text("Grant Push Notification Permission")
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Test Notification Lab",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Test Button 1: Gentle Nudge
          FilledTonalButton(
            onClick = {
              onTestPush(
                "Gentle Posture Nudge",
                "Unclench your jaw, soften your shoulders, and breathe deeply.",
                NudgeCategory.POSTURE.name,
                NudgePriority.GENTLE.name
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("test_gentle_push_button")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Test Gentle Nudge (Low Vibrate)")
              Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Test Button 2: Standard Reminder with Action Buttons
          FilledTonalButton(
            onClick = {
              onTestPush(
                "Hydration Reminder",
                "Drink a fresh glass of water to keep your focus sharp.",
                NudgeCategory.HYDRATION.name,
                NudgePriority.NORMAL.name
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("test_normal_push_button")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Test Habit Nudge (Sound & Action Buttons)")
              Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Test Button 3: Priority Nudge
          FilledTonalButton(
            onClick = {
              onTestPush(
                "Time to Step Away",
                "You've been working continuously. Step away for 5 minutes.",
                NudgeCategory.FOCUS.name,
                NudgePriority.URGENT.name
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("test_urgent_push_button")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Test Priority Nudge (High Attention)")
              Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    // Recent Activity History Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Recent Activity Log",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "${recentLogs.size} logs",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (recentLogs.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No activity logs yet. Complete or test nudges to see them recorded here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(recentLogs.take(15), key = { it.id }) { log ->
        LogItemRow(log = log)
      }
    }

    // Philosophy Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "The Science of Nudges",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Unlike intrusive alarms that cause friction, gentle cues work with your natural rhythm—nudging you toward mindfulness, health, and calm consistency.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun LogItemRow(log: NudgeLog) {
  val formattedTime = remember(log.timestamp) {
    val dt = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault())
    dt.format(DateTimeFormatter.ofPattern("MMM d • h:mm a"))
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
              when (log.action) {
                "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                "SNOOZED" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (log.action) {
              "COMPLETED" -> Icons.Outlined.Check
              "SNOOZED" -> Icons.Outlined.Snooze
              else -> Icons.Outlined.Notifications
            },
            contentDescription = null,
            tint = when (log.action) {
              "COMPLETED" -> MaterialTheme.colorScheme.primary
              "SNOOZED" -> MaterialTheme.colorScheme.secondary
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = log.nudgeTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = when (log.action) {
          "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
          "SNOOZED" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
          else -> MaterialTheme.colorScheme.surfaceVariant
        }
      ) {
        Text(
          text = log.action,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = when (log.action) {
            "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
            "SNOOZED" -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}
