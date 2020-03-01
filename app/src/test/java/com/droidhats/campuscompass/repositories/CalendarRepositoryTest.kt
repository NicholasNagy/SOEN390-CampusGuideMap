package com.droidhats.campuscompass.repositories

import android.os.Build
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.models.CalendarEvent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/*
* Let it be noted that there is a portion of calendar repository
* that can't be unit tested without actually providing calendar information through
* the calendar provider. The rest of this testing can be done through unit tests
* */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CalendarRepositoryTest {

    private val calendarRepository: CalendarRepository = CalendarRepository.getInstance()

    @Test
    fun testSingleton() {
        val testCalRepository: CalendarRepository = CalendarRepository.getInstance()
        val cal1: Calendar = Calendar(
            "123",
            "Account",
            "Type",
            "Display",
            "Owner",
            "color",
            true,
            arrayListOf<CalendarEvent>()
        )

        // won't set any actual calendars because the resolver.query returns null
        testCalRepository.setCalendars(mutableMapOf<String, Calendar>("cal1" to cal1))

        // asserts that the value of one changed to an instance is the same referenced
        // by another instance
        Assert.assertEquals(
            testCalRepository.getCalendars(RuntimeEnvironment.systemContext).value,
            calendarRepository.getCalendars(RuntimeEnvironment.systemContext).value
        )
    }

    @Test
    fun testNullQuery() {

        val calendars: MutableMap<String, Calendar>? =
            calendarRepository.getCalendars(RuntimeEnvironment.systemContext).value

        val emptyMap: MutableMap<String, Calendar> = mutableMapOf<String, Calendar>()

        Assert.assertEquals(calendars, emptyMap)
    }
}