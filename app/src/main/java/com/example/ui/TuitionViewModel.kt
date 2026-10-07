package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TuitionDatabase
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.data.repository.TuitionRepository
import com.example.util.AlarmSoundPlayer
import com.example.util.NextClassInfo
import com.example.util.NotificationHelper
import com.example.util.TimeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

enum class RoutineTab {
    DAY,
    WEEK,
    TEACHERS,
    SETTINGS
}

data class ActiveAlarmItem(
    val key: String,
    val tuitionClass: TuitionClass,
    val teacher: Teacher?,
    val triggerTimeMillis: Long,
    val classStartMillis: Long,
    val classEndMillis: Long,
    val ringingStartedMillis: Long = System.currentTimeMillis()
)

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TuitionRepository
    init {
        val db = TuitionDatabase.getDatabase(application)
        repository = TuitionRepository(db.tuitionDao(), application)
    }

    val classes: StateFlow<List<TuitionClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val remindersEnabled: StateFlow<Boolean> = repository.remindersEnabled
    val themeMode: StateFlow<String> = repository.themeMode

    private val _selectedTab = MutableStateFlow(RoutineTab.DAY)
    val selectedTab = _selectedTab.asStateFlow()

    // Default to today's day of week (Calendar.SUNDAY = 1 -> 0, etc.)
    private val todayDayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let {
        if (it < 0) 6 else it
    }
    private val _selectedDay = MutableStateFlow(todayDayOfWeek)
    val selectedDay = _selectedDay.asStateFlow()

    private val _nextClassInfo = MutableStateFlow<NextClassInfo?>(null)
    val nextClassInfo = _nextClassInfo.asStateFlow()

    private val _editingClass = MutableStateFlow<TuitionClass?>(null)
    val editingClass = _editingClass.asStateFlow()

    private val _isCreatingNewClass = MutableStateFlow(false)
    val isCreatingNewClass = _isCreatingNewClass.asStateFlow()

    private val _editingTeacher = MutableStateFlow<Teacher?>(null)
    val editingTeacher = _editingTeacher.asStateFlow()

    private val _isCreatingNewTeacher = MutableStateFlow(false)
    val isCreatingNewTeacher = _isCreatingNewTeacher.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    private val _activeAlarms = MutableStateFlow<List<ActiveAlarmItem>>(emptyList())
    val activeAlarms = _activeAlarms.asStateFlow()

    private val _isTestAlarmRinging = MutableStateFlow(false)
    val isTestAlarmRinging = _isTestAlarmRinging.asStateFlow()

    private val firedAlarmsKeys = mutableSetOf<String>()
    private val snoozedAlarms = mutableMapOf<String, Long>()

    init {
        // Seed initial data if empty so the user immediately gets a populated schedule
        viewModelScope.launch {
            delay(300)
            repository.seedSampleDataIfEmpty(classes.value.size)
        }

        // Ticker loop for next-class countdown and alarms checking
        viewModelScope.launch {
            while (isActive) {
                updateNextClass()
                checkAlarms()
                delay(1000)
            }
        }
    }

    fun selectTab(tab: RoutineTab) {
        _selectedTab.value = tab
    }

    fun selectDay(dayOfWeek: Int) {
        _selectedDay.value = dayOfWeek
    }

    fun openNewClass(dayOfWeek: Int = _selectedDay.value) {
        val sameDayClasses = classes.value.filter { it.day == dayOfWeek }.sortedBy { it.start }
        val defaultStart = if (sameDayClasses.isNotEmpty()) {
            sameDayClasses.last().end
        } else {
            "16:00"
        }
        val startMin = TimeUtil.parseToMinutes(defaultStart)
        val defaultEnd = TimeUtil.minutesToTimeStr(startMin + 90)

        _editingClass.value = TuitionClass(
            day = dayOfWeek,
            start = defaultStart,
            end = defaultEnd,
            subject = "",
            teacherId = null,
            note = "",
            remindMinutes = 30 // Default 30 min before class
        )
        _isCreatingNewClass.value = true
    }

    fun openEditClass(tuitionClass: TuitionClass) {
        _editingClass.value = tuitionClass
        _isCreatingNewClass.value = false
    }

    fun closeClassEditor() {
        _editingClass.value = null
        _isCreatingNewClass.value = false
    }

    fun saveClass(tuitionClass: TuitionClass) {
        viewModelScope.launch {
            val cleanSubject = tuitionClass.subject.trim().ifEmpty { "Class" }
            val startMin = TimeUtil.parseToMinutes(tuitionClass.start)
            var endMin = TimeUtil.parseToMinutes(tuitionClass.end)
            if (endMin <= startMin) {
                endMin = (startMin + 60).coerceAtMost(23 * 60 + 59)
            }
            val validated = tuitionClass.copy(
                subject = cleanSubject,
                end = TimeUtil.minutesToTimeStr(endMin)
            )
            repository.saveClass(validated)
            _editingClass.value = null
            _isCreatingNewClass.value = false
            showStatus("Class saved with ${validated.remindMinutes}m reminder")
            updateNextClass()
        }
    }

    fun deleteClass(tuitionClass: TuitionClass) {
        viewModelScope.launch {
            repository.deleteClass(tuitionClass)
            _editingClass.value = null
            _isCreatingNewClass.value = false
            showStatus("Class deleted")
            updateNextClass()
        }
    }

    fun openNewTeacher() {
        _editingTeacher.value = Teacher(
            name = "",
            teaches = "",
            address = "",
            phone = ""
        )
        _isCreatingNewTeacher.value = true
    }

    fun openEditTeacher(teacher: Teacher) {
        _editingTeacher.value = teacher
        _isCreatingNewTeacher.value = false
    }

    fun closeTeacherEditor() {
        _editingTeacher.value = null
        _isCreatingNewTeacher.value = false
    }

    fun saveTeacher(teacher: Teacher) {
        viewModelScope.launch {
            val cleanName = teacher.name.trim().ifEmpty { "Teacher" }
            repository.saveTeacher(teacher.copy(name = cleanName))
            _editingTeacher.value = null
            _isCreatingNewTeacher.value = false
            showStatus("Teacher profile saved")
            updateNextClass()
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            _editingTeacher.value = null
            _isCreatingNewTeacher.value = false
            showStatus("Teacher removed")
            updateNextClass()
        }
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun toggleReminders() {
        val current = remindersEnabled.value
        repository.setRemindersEnabled(!current)
        if (current) {
            // Turning off: stop all ringing alarms
            stopAllAlarms()
        }
        showStatus(if (!current) "Reminders turned on" else "Reminders turned off")
    }

    /**
     * Test the loud alarm sound (wake up sound) directly.
     */
    fun startTestAlarm(context: Context) {
        _isTestAlarmRinging.value = true
        AlarmSoundPlayer.startLoudAlarm(context)
        NotificationHelper.showTestNotification(context)
        showStatus("Loud alarm test started! Tap 'Stop' to silence.")
    }

    fun stopTestAlarm(context: Context) {
        _isTestAlarmRinging.value = false
        AlarmSoundPlayer.stopAlarm(context)
        showStatus("Alarm test stopped.")
    }

    /**
     * User taps "STOP ALARM" (I'm awake!):
     * Completely stops sound and silences alarm permanently for this session.
     */
    fun dismissAlarm(key: String) {
        _activeAlarms.value = _activeAlarms.value.filter { it.key != key }
        snoozedAlarms.remove(key)
        if (_activeAlarms.value.isEmpty() && !_isTestAlarmRinging.value) {
            AlarmSoundPlayer.stopAlarm(getApplication())
        }
        showStatus("Alarm stopped. You are on time!")
    }

    /**
     * User taps "SNOOZE 5 MIN":
     * Stops sound now, rings again after a 5 min gap.
     */
    fun snoozeAlarm(key: String) {
        val now = System.currentTimeMillis()
        snoozedAlarms[key] = now + 5 * 60 * 1000 // 5 minutes
        _activeAlarms.value = _activeAlarms.value.filter { it.key != key }
        if (_activeAlarms.value.isEmpty() && !_isTestAlarmRinging.value) {
            AlarmSoundPlayer.stopAlarm(getApplication())
        }
        showStatus("Alarm snoozed. Will ring again in 5 minutes.")
    }

    private fun stopAllAlarms() {
        _activeAlarms.value = emptyList()
        snoozedAlarms.clear()
        _isTestAlarmRinging.value = false
        AlarmSoundPlayer.stopAlarm(getApplication())
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            showStatus("Demo schedule loaded with 30m reminders")
            updateNextClass()
        }
    }

    private fun updateNextClass() {
        _nextClassInfo.value = TimeUtil.findNextClass(
            classes = classes.value,
            teachers = teachers.value,
            nowCalendar = Calendar.getInstance()
        )
    }

    private fun checkAlarms() {
        if (!remindersEnabled.value) {
            if (_activeAlarms.value.isNotEmpty()) {
                _activeAlarms.value = emptyList()
                AlarmSoundPlayer.stopAlarm(getApplication())
            }
            return
        }

        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1

        val currentClasses = classes.value
        val teacherMap = teachers.value.associateBy { it.id }

        val activeList = _activeAlarms.value.toMutableList()

        // 1. Check auto-snooze for existing ringing alarms:
        // If an alarm has been ringing for >= 60 seconds without the user stopping it,
        // automatically snooze it for 5 minutes and stop the audio during the gap!
        val it = activeList.iterator()
        var autoSnoozeTriggered = false
        while (it.hasNext()) {
            val alarm = it.next()
            if (nowMillis - alarm.ringingStartedMillis >= 60 * 1000L) {
                // Not stopped by user -> Alarm after 5 min gap!
                snoozedAlarms[alarm.key] = nowMillis + 5 * 60 * 1000L
                it.remove()
                autoSnoozeTriggered = true
            }
        }
        if (autoSnoozeTriggered) {
            _activeAlarms.value = activeList
            if (activeList.isEmpty() && !_isTestAlarmRinging.value) {
                AlarmSoundPlayer.stopAlarm(getApplication())
                showStatus("Alarm not stopped: Paused for 5 min gap, will ring again!")
            }
        }

        // 2. Check scheduled and snoozed alarms for Today and Tomorrow
        for (offset in 0..1) {
            val checkDay = (currentDay + offset) % 7
            val checkClasses = currentClasses.filter { it.day == checkDay && it.remindMinutes > 0 }

            for (c in checkClasses) {
                val startMin = TimeUtil.parseToMinutes(c.start)
                val endMin = TimeUtil.parseToMinutes(c.end)

                val classStartCal = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, startMin / 60)
                    set(Calendar.MINUTE, startMin % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val classEndCal = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, endMin / 60)
                    set(Calendar.MINUTE, endMin % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val startMillis = classStartCal.timeInMillis
                val endMillis = classEndCal.timeInMillis
                val reminderMillis = startMillis - (c.remindMinutes * 60 * 1000L)
                val alarmKey = "${c.id}_${startMillis}_${c.remindMinutes}"

                // Check if snoozed alarm time has arrived (5 min gap elapsed)
                val snoozeUntil = snoozedAlarms[alarmKey]
                if (snoozeUntil != null && nowMillis >= snoozeUntil && nowMillis < startMillis) {
                    snoozedAlarms.remove(alarmKey)
                    if (activeList.none { it.key == alarmKey }) {
                        activeList.add(
                            ActiveAlarmItem(
                                key = alarmKey,
                                tuitionClass = c,
                                teacher = teacherMap[c.teacherId],
                                triggerTimeMillis = nowMillis,
                                classStartMillis = startMillis,
                                classEndMillis = endMillis,
                                ringingStartedMillis = nowMillis
                            )
                        )
                        // Trigger loud sound again
                        AlarmSoundPlayer.startLoudAlarm(getApplication())
                        val teacher = teacherMap[c.teacherId]
                        val remainingMins = ((startMillis - nowMillis) / 60000).toInt().coerceAtLeast(1)
                        NotificationHelper.showClassReminder(
                            context = getApplication(),
                            subject = c.subject,
                            startTime = TimeUtil.format12HourString(c.start),
                            teacherName = teacher?.name,
                            address = teacher?.address,
                            minutesUntilStart = remainingMins
                        )
                    }
                }

                // If within reminder window and hasn't fired yet
                if (nowMillis in reminderMillis until startMillis && !firedAlarmsKeys.contains(alarmKey)) {
                    firedAlarmsKeys.add(alarmKey)
                    val teacher = teacherMap[c.teacherId]

                    // Send high-priority notification with sound
                    NotificationHelper.showClassReminder(
                        context = getApplication(),
                        subject = c.subject,
                        startTime = TimeUtil.format12HourString(c.start),
                        teacherName = teacher?.name,
                        address = teacher?.address,
                        minutesUntilStart = c.remindMinutes
                    )

                    // Start continuous loud alarm sound so user wakes up
                    AlarmSoundPlayer.startLoudAlarm(getApplication())

                    if (activeList.none { it.key == alarmKey }) {
                        activeList.add(
                            ActiveAlarmItem(
                                key = alarmKey,
                                tuitionClass = c,
                                teacher = teacher,
                                triggerTimeMillis = reminderMillis,
                                classStartMillis = startMillis,
                                classEndMillis = endMillis,
                                ringingStartedMillis = nowMillis
                            )
                        )
                    }
                }
            }
        }

        // Clean up alarms whose class has ended
        val endedAlarms = activeList.filter { nowMillis >= it.classEndMillis }
        if (endedAlarms.isNotEmpty()) {
            activeList.removeAll(endedAlarms)
            if (activeList.isEmpty() && !_isTestAlarmRinging.value) {
                AlarmSoundPlayer.stopAlarm(getApplication())
            }
        }

        _activeAlarms.value = activeList
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        AlarmSoundPlayer.stopAlarm(getApplication())
    }
}
