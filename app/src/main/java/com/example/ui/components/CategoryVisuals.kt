package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.NudgeCategory
import com.example.ui.theme.CategoryConnection
import com.example.ui.theme.CategoryFocus
import com.example.ui.theme.CategoryHabit
import com.example.ui.theme.CategoryHydration
import com.example.ui.theme.CategoryMindfulness
import com.example.ui.theme.CategoryMovement
import com.example.ui.theme.CategoryPosture

object CategoryVisuals {
  fun getIcon(category: NudgeCategory): ImageVector {
    return when (category) {
      NudgeCategory.MINDFULNESS -> Icons.Outlined.SelfImprovement
      NudgeCategory.HYDRATION -> Icons.Outlined.WaterDrop
      NudgeCategory.POSTURE -> Icons.Outlined.AccessibilityNew
      NudgeCategory.FOCUS -> Icons.Outlined.Lightbulb
      NudgeCategory.MOVEMENT -> Icons.Outlined.DirectionsWalk
      NudgeCategory.CONNECTION -> Icons.Outlined.Favorite
      NudgeCategory.HABIT -> Icons.Outlined.CheckCircle
      NudgeCategory.CUSTOM -> Icons.Outlined.NotificationsActive
    }
  }

  fun getColor(category: NudgeCategory): Color {
    return when (category) {
      NudgeCategory.MINDFULNESS -> CategoryMindfulness
      NudgeCategory.HYDRATION -> CategoryHydration
      NudgeCategory.POSTURE -> CategoryPosture
      NudgeCategory.FOCUS -> CategoryFocus
      NudgeCategory.MOVEMENT -> CategoryMovement
      NudgeCategory.CONNECTION -> CategoryConnection
      NudgeCategory.HABIT -> CategoryHabit
      NudgeCategory.CUSTOM -> Color(0xFF00695C)
    }
  }
}
