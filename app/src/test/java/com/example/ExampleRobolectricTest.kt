package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NudgeDatabase
import com.example.data.model.Nudge
import com.example.data.model.NudgeCategory
import com.example.data.model.NudgePriority
import com.example.data.model.ScheduleType
import com.example.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nudge", appName)
  }

  @Test
  fun `notification channels are created properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    NotificationHelper.createNotificationChannels(context)

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channels = notificationManager.notificationChannels

    assertTrue("Should have created channels", channels.isNotEmpty())
    val channelIds = channels.map { it.id }
    assertTrue(channelIds.contains(NotificationHelper.CHANNEL_GENTLE_ID))
    assertTrue(channelIds.contains(NotificationHelper.CHANNEL_NORMAL_ID))
    assertTrue(channelIds.contains(NotificationHelper.CHANNEL_URGENT_ID))
  }

  @Test
  fun `default nudges provide balanced habit templates`() {
    val defaultNudges = NudgeDatabase.getDefaultNudges()
    assertTrue(defaultNudges.size >= 5)

    val hydrationNudge = defaultNudges.find { it.category == NudgeCategory.HYDRATION.name }
    assertNotNull(hydrationNudge)
    assertEquals(ScheduleType.INTERVAL.name, hydrationNudge?.scheduleType)

    val postureNudge = defaultNudges.find { it.category == NudgeCategory.POSTURE.name }
    assertNotNull(postureNudge)
  }

  @Test
  fun `nudge completion status and formatting`() {
    val todayEpochDay = LocalDate.now().toEpochDay()
    val nudge = Nudge(
      id = 1,
      title = "Drink water",
      message = "Stay hydrated",
      category = NudgeCategory.HYDRATION.name,
      scheduleType = ScheduleType.TIME_OF_DAY.name,
      hour = 9,
      minute = 30,
      daysOfWeek = "1,2,3,4,5,6,7",
      lastCompletedEpochDay = todayEpochDay
    )

    assertTrue("Should be completed today", nudge.isCompletedToday)
    assertEquals("9:30 AM", nudge.formatTime())
    assertEquals(7, nudge.getDaysList().size)
  }
}
