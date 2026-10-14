package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.ui.components.CategoryVisuals

@Composable
fun AllNudgesScreen(
  nudges: List<Nudge>,
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit,
  onToggleNudge: (Long, Boolean) -> Unit,
  onEditNudge: (Nudge) -> Unit,
  onDeleteNudge: (Nudge) -> Unit,
  onTriggerPush: (Nudge) -> Unit,
  onAddNewNudge: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeCount = nudges.count { it.isEnabled }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
      item {
        Column(modifier = Modifier.padding(bottom = 6.dp)) {
          Text(
            text = "Your Nudges",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$activeCount active of ${nudges.size} total",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Filter chips row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("All") }
          )
          NudgeCategory.entries.forEach { cat ->
            FilterChip(
              selected = selectedCategory.equals(cat.name, ignoreCase = true),
              onClick = {
                onSelectCategory(if (selectedCategory.equals(cat.name, ignoreCase = true)) null else cat.name)
              },
              label = { Text(cat.displayName) },
              leadingIcon = {
                Icon(
                  imageVector = CategoryVisuals.getIcon(cat),
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = CategoryVisuals.getColor(cat)
                )
              }
            )
          }
        }
      }

      if (nudges.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No nudges in this category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Create a custom nudge to stay balanced throughout your day.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      } else {
        items(nudges, key = { it.id }) { nudge ->
          NudgeListItem(
            nudge = nudge,
            onToggle = { isEnabled -> onToggleNudge(nudge.id, isEnabled) },
            onEdit = { onEditNudge(nudge) },
            onDelete = { onDeleteNudge(nudge) },
            onTriggerPush = { onTriggerPush(nudge) }
          )
        }
      }
    }

    // Floating Action Button to Add Nudge
    FloatingActionButton(
      onClick = onAddNewNudge,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(24.dp)
        .testTag("add_nudge_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add new nudge")
    }
  }
}

@Composable
fun NudgeListItem(
  nudge: Nudge,
  onToggle: (Boolean) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onTriggerPush: () -> Unit
) {
  val categoryEnum = NudgeCategory.fromName(nudge.category)
  val categoryColor = CategoryVisuals.getColor(categoryEnum)
  var showMenu by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (nudge.isEnabled) MaterialTheme.colorScheme.surface
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (nudge.isEnabled) 2.dp else 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("nudge_card_${nudge.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Icon
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(categoryColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = CategoryVisuals.getIcon(categoryEnum),
            contentDescription = categoryEnum.displayName,
            tint = categoryColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Category
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = nudge.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (nudge.isEnabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = categoryEnum.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = categoryColor,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Enable / Disable Switch
        Switch(
          checked = nudge.isEnabled,
          onCheckedChange = onToggle,
          modifier = Modifier.testTag("toggle_nudge_${nudge.id}")
        )

        // Overflow menu
        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.testTag("nudge_menu_${nudge.id}")
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Send Push Nudge Now") },
              leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
              onClick = {
                showMenu = false
                onTriggerPush()
              }
            )
            DropdownMenuItem(
              text = { Text("Edit Nudge") },
              leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
              onClick = {
                showMenu = false
                onEdit()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete Nudge") },
              leadingIcon = { Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }

      if (nudge.message.isNotBlank()) {
        Text(
          text = nudge.message,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Timing & Days tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Text(
            text = nudge.formatScheduleSummary(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        if (nudge.currentStreak > 0) {
          Text(
            text = "🔥 ${nudge.currentStreak} day streak (Best: ${nudge.bestStreak})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
