package com.example.data.repository

import android.content.Context
import com.example.data.local.NudgeDao
import com.example.data.model.Nudge
import com.example.data.model.NudgeLog
import com.example.notifications.NudgeScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class NudgeRepository(
  private val context: Context,
  private val nudgeDao: NudgeDao
) {
  val allNudges: Flow<List<Nudge>> = nudgeDao.getAllNudges()
  val recentLogs: Flow<List<NudgeLog>> = nudgeDao.getRecentLogs(limit = 60)

  suspend fun getNudge(id: Long): Nudge? = withContext(Dispatchers.IO) {
    nudgeDao.getNudgeById(id)
  }

  suspend fun addNudge(nudge: Nudge): Long = withContext(Dispatchers.IO) {
    val id = nudgeDao.insertNudge(nudge)
    val saved = nudge.copy(id = id)
    if (saved.isEnabled) {
      NudgeScheduler.scheduleNextNudge(context, saved)
    }
    id
  }

  suspend fun updateNudge(nudge: Nudge) = withContext(Dispatchers.IO) {
    nudgeDao.updateNudge(nudge)
    if (nudge.isEnabled) {
      NudgeScheduler.scheduleNextNudge(context, nudge)
    } else {
      NudgeScheduler.cancelNudge(context, nudge.id)
    }
  }

  suspend fun deleteNudge(nudge: Nudge) = withContext(Dispatchers.IO) {
    NudgeScheduler.cancelNudge(context, nudge.id)
    nudgeDao.deleteNudge(nudge)
  }

  suspend fun toggleNudge(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
    nudgeDao.setNudgeEnabled(id, isEnabled)
    val nudge = nudgeDao.getNudgeById(id) ?: return@withContext
    if (isEnabled) {
      NudgeScheduler.scheduleNextNudge(context, nudge)
    } else {
      NudgeScheduler.cancelNudge(context, id)
    }
  }

  suspend fun completeNudge(nudge: Nudge, note: String? = null) = withContext(Dispatchers.IO) {
    val todayEpochDay = LocalDate.now().toEpochDay()
    val isConsecutive = nudge.lastCompletedEpochDay == todayEpochDay - 1
    val newStreak = if (nudge.lastCompletedEpochDay == todayEpochDay) {
      nudge.currentStreak
    } else if (isConsecutive) {
      nudge.currentStreak + 1
    } else {
      1
    }
    val newBestStreak = maxOf(nudge.bestStreak, newStreak)

    nudgeDao.updateCompletion(
      id = nudge.id,
      streak = newStreak,
      bestStreak = newBestStreak,
      epochDay = todayEpochDay
    )

    nudgeDao.insertLog(
      NudgeLog(
        nudgeId = nudge.id,
        nudgeTitle = nudge.title,
        category = nudge.category,
        action = "COMPLETED",
        note = note
      )
    )

    // Schedule next run
    NudgeScheduler.scheduleNextNudge(context, nudge)
  }

  suspend fun logSnooze(nudgeId: Long, title: String, category: String) = withContext(Dispatchers.IO) {
    nudgeDao.insertLog(
      NudgeLog(
        nudgeId = nudgeId,
        nudgeTitle = title,
        category = category,
        action = "SNOOZED"
      )
    )
  }

  suspend fun logManualTrigger(nudge: Nudge) = withContext(Dispatchers.IO) {
    nudgeDao.insertLog(
      NudgeLog(
        nudgeId = nudge.id,
        nudgeTitle = nudge.title,
        category = nudge.category,
        action = "TRIGGERED"
      )
    )
  }

  suspend fun rescheduleAllActive() = withContext(Dispatchers.IO) {
    val active = nudgeDao.getActiveNudgesSync()
    for (nudge in active) {
      NudgeScheduler.scheduleNextNudge(context, nudge)
    }
  }
}
