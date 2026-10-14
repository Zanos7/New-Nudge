package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nudge_logs")
data class NudgeLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val nudgeId: Long,
  val nudgeTitle: String,
  val category: String,
  val timestamp: Long = System.currentTimeMillis(),
  val action: String = "COMPLETED", // COMPLETED, SNOOZED, DISMISSED, TRIGGERED
  val note: String? = null
)
