package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TuitionClass
import com.example.util.SubjectColorUtil
import com.example.util.TimeUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tuition Routine", appName)
    }

    @Test
    fun `test time formatting and parsing`() {
        assertEquals(960, TimeUtil.parseToMinutes("16:00"))
        assertEquals("16:00", TimeUtil.minutesToTimeStr(960))

        val f12 = TimeUtil.formatTo12Hour("16:30")
        assertEquals("4:30", f12.timeDigits)
        assertEquals("PM", f12.amPm)

        val fMorning = TimeUtil.formatTo12Hour("09:05")
        assertEquals("9:05", fMorning.timeDigits)
        assertEquals("AM", fMorning.amPm)
    }

    @Test
    fun `test subject colors generator`() {
        val physicsColors = SubjectColorUtil.getColorsForSubject("Physics 1st Paper", isDark = false)
        assertNotNull(physicsColors.background)
        assertNotNull(physicsColors.text)

        val mathColors = SubjectColorUtil.getColorsForSubject("গণিত", isDark = true)
        assertNotNull(mathColors.background)
        assertNotNull(mathColors.text)
    }
}
