package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.NudgePriority
import com.example.data.model.ScheduleType
import com.example.ui.components.CategoryVisuals

data class NudgeTemplate(
  val title: String,
  val message: String,
  val category: NudgeCategory,
  val scheduleType: ScheduleType,
  val hour: Int = 10,
  val minute: Int = 0,
  val intervalMinutes: Int = 60
)

val PRESET_TEMPLATES = listOf(
  NudgeTemplate(
    title = "Hydrate Now",
    message = "Drink a large glass of refreshing water.",
    category = NudgeCategory.HYDRATION,
    scheduleType = ScheduleType.INTERVAL,
    intervalMinutes = 90
  ),
  NudgeTemplate(
    title = "Posture & Shoulders",
    message = "Sit tall, pull shoulders down, and relax your forehead.",
    category = NudgeCategory.POSTURE,
    scheduleType = ScheduleType.INTERVAL,
    intervalMinutes = 60
  ),
  NudgeTemplate(
    title = "Box Breathing Reset",
    message = "Inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat 3 times.",
    category = NudgeCategory.MINDFULNESS,
    scheduleType = ScheduleType.TIME_OF_DAY,
    hour = 14,
    minute = 30
  ),
  NudgeTemplate(
    title = "Walk & Stretch",
    message = "Step away from the screen for 2 minutes to stretch your legs.",
    category = NudgeCategory.MOVEMENT,
    scheduleType = ScheduleType.TIME_OF_DAY,
    hour = 11,
    minute = 15
  ),
  NudgeTemplate(
    title = "Reach Out to Someone",
    message = "Send a thoughtful text or call to someone you care about.",
    category = NudgeCategory.CONNECTION,
    scheduleType = ScheduleType.TIME_OF_DAY,
    hour = 18,
    minute = 0
  )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NudgeEditorDialog(
  nudge: Nudge?,
  onDismiss: () -> Unit,
  onSave: (
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
  ) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var title by remember { mutableStateOf(nudge?.title ?: "") }
  var message by remember { mutableStateOf(nudge?.message ?: "") }
  var category by remember { mutableStateOf(nudge?.let { NudgeCategory.fromName(it.category) } ?: NudgeCategory.MINDFULNESS) }
  var scheduleType by remember { mutableStateOf(nudge?.let { ScheduleType.fromName(it.scheduleType) } ?: ScheduleType.TIME_OF_DAY) }

  var hour by remember { mutableIntStateOf(nudge?.hour ?: 10) }
  var minute by remember { mutableIntStateOf(nudge?.minute ?: 0) }
  var intervalMinutes by remember { mutableIntStateOf(nudge?.intervalMinutes ?: 60) }
  var startHour by remember { mutableIntStateOf(nudge?.startHour ?: 9) }
  var endHour by remember { mutableIntStateOf(nudge?.endHour ?: 18) }

  val existingDays = remember { nudge?.getDaysList() ?: listOf(1, 2, 3, 4, 5, 6, 7) }
  var selectedDays by remember { mutableStateOf(existingDays.toSet()) }

  var priority by remember { mutableStateOf(nudge?.let { NudgePriority.fromName(it.priority) } ?: NudgePriority.NORMAL) }
  var soundEnabled by remember { mutableStateOf(nudge?.soundEnabled ?: true) }
  var vibrateEnabled by remember { mutableStateOf(nudge?.vibrateEnabled ?: true) }

  var isError by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (nudge == null) "Create New Nudge" else "Edit Nudge",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Set up your gentle behavioral prompt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close editor")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Preset Templates (only when creating new)
      if (nudge == null) {
        Text(
          text = "Quick Presets",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PRESET_TEMPLATES.forEach { template ->
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier
                .clickable {
                  title = template.title
                  message = template.message
                  category = template.category
                  scheduleType = template.scheduleType
                  hour = template.hour
                  minute = template.minute
                  intervalMinutes = template.intervalMinutes
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = CategoryVisuals.getIcon(template.category),
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = CategoryVisuals.getColor(template.category)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = template.title,
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Title Input
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          if (it.isNotBlank()) isError = false
        },
        label = { Text("Nudge Title") },
        placeholder = { Text("e.g., Drink water, Posture check") },
        isError = isError,
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("nudge_title_input")
      )
      if (isError) {
        Text(
          text = "Title is required",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 8.dp, top = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Motivational Cue / Message
      OutlinedTextField(
        value = message,
        onValueChange = { message = it },
        label = { Text("Gentle Prompt / Message") },
        placeholder = { Text("e.g., Take a deep breath and relax your shoulders") },
        minLines = 2,
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("nudge_message_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Category Selector
      Text(
        text = "Category",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        NudgeCategory.entries.forEach { cat ->
          val isSelected = category == cat
          FilterChip(
            selected = isSelected,
            onClick = { category = cat },
            label = { Text(cat.displayName) },
            leadingIcon = {
              Icon(
                imageVector = CategoryVisuals.getIcon(cat),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else CategoryVisuals.getColor(cat)
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Schedule Type
      Text(
        text = "Timing & Frequency",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ScheduleType.entries.forEach { type ->
          FilterChip(
            selected = scheduleType == type,
            onClick = { scheduleType = type },
            label = { Text(type.displayName) }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Timing Details based on schedule type
      when (scheduleType) {
        ScheduleType.TIME_OF_DAY -> {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Reminder Time: ${formatTime(hour, minute)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text("Hour: $hour (24h)", style = MaterialTheme.typography.labelSmall)
              Slider(
                value = hour.toFloat(),
                onValueChange = { hour = it.toInt() },
                valueRange = 0f..23f,
                steps = 22
              )
              Text("Minute: $minute", style = MaterialTheme.typography.labelSmall)
              Slider(
                value = minute.toFloat(),
                onValueChange = { minute = (it / 5).toInt() * 5 },
                valueRange = 0f..55f,
                steps = 10
              )
            }
          }
        }

        ScheduleType.INTERVAL -> {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Repeat every: $intervalMinutes minutes",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(30, 45, 60, 90, 120).forEach { mins ->
                  FilterChip(
                    selected = intervalMinutes == mins,
                    onClick = { intervalMinutes = mins },
                    label = { Text("${mins}m") }
                  )
                }
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Active hours: ${startHour}:00 to ${endHour}:00",
                style = MaterialTheme.typography.bodySmall
              )
              Slider(
                value = startHour.toFloat(),
                onValueChange = { startHour = it.toInt().coerceAtMost(endHour - 1) },
                valueRange = 6f..14f,
                steps = 7
              )
            }
          }
        }

        ScheduleType.RANDOM_WINDOW -> {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Gentle surprise nudge between ${startHour}:00 - ${endHour}:00",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "Triggers unpredictably to keep habits natural and mindful.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Days of Week
      Text(
        text = "Active Days",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      val days = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        days.forEach { (label, dayNum) ->
          val isSelected = selectedDays.contains(dayNum)
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              )
              .clickable {
                selectedDays = if (isSelected) {
                  if (selectedDays.size > 1) selectedDays - dayNum else selectedDays
                } else {
                  selectedDays + dayNum
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Notification Priority & Feedback
      Text(
        text = "Notification Style",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        NudgePriority.entries.forEach { p ->
          FilterChip(
            selected = priority == p,
            onClick = { priority = p },
            label = { Text(p.displayName) }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Play sound with push notification", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Vibrate on notification", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(onClick = onDismiss) {
          Text("Cancel")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
          onClick = {
            if (title.isBlank()) {
              isError = true
              return@Button
            }
            val daysCsv = selectedDays.sorted().joinToString(",")
            onSave(
              nudge?.id ?: 0L,
              title,
              message,
              category,
              scheduleType,
              hour,
              minute,
              intervalMinutes,
              startHour,
              endHour,
              daysCsv,
              priority,
              soundEnabled,
              vibrateEnabled
            )
          },
          modifier = Modifier.testTag("save_nudge_button")
        ) {
          Text(if (nudge == null) "Create Nudge" else "Save Changes")
        }
      }
    }
  }
}

private fun formatTime(hour: Int, minute: Int): String {
  val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
  val m = String.format("%02d", minute)
  val amPm = if (hour < 12) "AM" else "PM"
  return "$h:$m $amPm"
}
