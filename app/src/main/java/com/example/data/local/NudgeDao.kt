package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Nudge
import com.example.data.model.NudgeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NudgeDao {
  @Query("SELECT * FROM nudges ORDER BY isEnabled DESC, hour ASC, minute ASC")
  fun getAllNudges(): Flow<List<Nudge>>

  @Query("SELECT * FROM nudges ORDER BY hour ASC, minute ASC")
  suspend fun getAllNudgesSync(): List<Nudge>

  @Query("SELECT * FROM nudges WHERE isEnabled = 1")
  suspend fun getActiveNudgesSync(): List<Nudge>

  @Query("SELECT * FROM nudges WHERE id = :id")
  suspend fun getNudgeById(id: Long): Nudge?

  @Query("SELECT * FROM nudges WHERE id = :id")
  fun getNudgeByIdFlow(id: Long): Flow<Nudge?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNudge(nudge: Nudge): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNudges(nudges: List<Nudge>)

  @Update
  suspend fun updateNudge(nudge: Nudge)

  @Delete
  suspend fun deleteNudge(nudge: Nudge)

  @Query("DELETE FROM nudges WHERE id = :id")
  suspend fun deleteNudgeById(id: Long)

  @Query("UPDATE nudges SET isEnabled = :isEnabled WHERE id = :id")
  suspend fun setNudgeEnabled(id: Long, isEnabled: Boolean)

  @Query("UPDATE nudges SET currentStreak = :streak, bestStreak = :bestStreak, lastCompletedEpochDay = :epochDay WHERE id = :id")
  suspend fun updateCompletion(id: Long, streak: Int, bestStreak: Int, epochDay: Long)

  // Nudge Logs
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: NudgeLog): Long

  @Query("SELECT * FROM nudge_logs ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentLogs(limit: Int = 50): Flow<List<NudgeLog>>

  @Query("SELECT COUNT(*) FROM nudge_logs WHERE action = 'COMPLETED'")
  fun getTotalCompletionsCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM nudge_logs WHERE action = 'COMPLETED' AND timestamp >= :startOfDayMs")
  fun getTodayCompletionsCount(startOfDayMs: Long): Flow<Int>
}
