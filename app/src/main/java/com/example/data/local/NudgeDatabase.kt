package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.data.model.NudgeLog
import com.example.data.model.NudgePriority
import com.example.data.model.ScheduleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [Nudge::class, NudgeLog::class],
  version = 1,
  exportSchema = false
)
abstract class NudgeDatabase : RoomDatabase() {
  abstract fun nudgeDao(): NudgeDao

  companion object {
    @Volatile
    private var INSTANCE: NudgeDatabase? = null

    fun getDatabase(context: Context): NudgeDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          NudgeDatabase::class.java,
          "nudge_database"
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                getDatabase(context).nudgeDao().insertNudges(getDefaultNudges())
              }
            }
          })
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    fun getDefaultNudges(): List<Nudge> {
      return listOf(
        Nudge(
          title = "Drink a glass of water",
          message = "Hydrate your body and sharpen your mind with cool water.",
          category = NudgeCategory.HYDRATION.name,
          scheduleType = ScheduleType.INTERVAL.name,
          intervalMinutes = 90,
          startHour = 9,
          endHour = 20,
          daysOfWeek = "1,2,3,4,5,6,7",
          isEnabled = true,
          priority = NudgePriority.NORMAL.name
        ),
        Nudge(
          title = "Posture check & jaw unclench",
          message = "Roll shoulders back, unclench your teeth, and let your neck relax.",
          category = NudgeCategory.POSTURE.name,
          scheduleType = ScheduleType.TIME_OF_DAY.name,
          hour = 11,
          minute = 0,
          daysOfWeek = "1,2,3,4,5,6,7",
          isEnabled = true,
          priority = NudgePriority.GENTLE.name
        ),
        Nudge(
          title = "Deep Breath & Reset",
          message = "Take 60 seconds: Inhale 4s, hold 4s, slow exhale 6s.",
          category = NudgeCategory.MINDFULNESS.name,
          scheduleType = ScheduleType.TIME_OF_DAY.name,
          hour = 14,
          minute = 30,
          daysOfWeek = "1,2,3,4,5,6,7",
          isEnabled = true,
          priority = NudgePriority.GENTLE.name
        ),
        Nudge(
          title = "20-20-20 Screen Break",
          message = "Look at an object 20 feet away for 20 seconds to soothe your eyes.",
          category = NudgeCategory.FOCUS.name,
          scheduleType = ScheduleType.INTERVAL.name,
          intervalMinutes = 60,
          startHour = 10,
          endHour = 18,
          daysOfWeek = "1,2,3,4,5",
          isEnabled = true,
          priority = NudgePriority.NORMAL.name
        ),
        Nudge(
          title = "Stand up & light stretch",
          message = "Step away from your desk, stretch your arms high, and shake out tension.",
          category = NudgeCategory.MOVEMENT.name,
          scheduleType = ScheduleType.TIME_OF_DAY.name,
          hour = 16,
          minute = 0,
          daysOfWeek = "1,2,3,4,5,6,7",
          isEnabled = true,
          priority = NudgePriority.NORMAL.name
        ),
        Nudge(
          title = "Evening gratitude reflection",
          message = "Acknowledge one small thing that went well or gave you peace today.",
          category = NudgeCategory.CONNECTION.name,
          scheduleType = ScheduleType.TIME_OF_DAY.name,
          hour = 21,
          minute = 0,
          daysOfWeek = "1,2,3,4,5,6,7",
          isEnabled = true,
          priority = NudgePriority.GENTLE.name
        )
      )
    }
  }
}
