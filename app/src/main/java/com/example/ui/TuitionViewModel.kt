package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TuitionDatabase
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.data.repository.TuitionRepository
import com.example.util.NextClassInfo
import com.example.util.NotificationHelper
import com.example.util.TimeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val classEndMillis: Long
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
            remindMinutes = 30
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
            showStatus("Class saved")
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
        showStatus(if (!current) "Reminders turned on" else "Reminders turned off")
    }

    fun testAlarm(context: Context) {
        NotificationHelper.showTestNotification(context)
        showStatus("Test reminder triggered with sound & vibration")
    }

    fun dismissAlarm(key: String) {
        _activeAlarms.value = _activeAlarms.value.filter { it.key != key }
    }

    fun snoozeAlarm(key: String) {
        val now = System.currentTimeMillis()
        snoozedAlarms[key] = now + 5 * 60 * 1000 // 5 minutes
        _activeAlarms.value = _activeAlarms.value.filter { it.key != key }
        showStatus("Will remind again in 5 minutes")
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            showStatus("Demo schedule loaded")
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
            _activeAlarms.value = emptyList()
            return
        }

        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1

        val currentClasses = classes.value
        val teacherMap = teachers.value.associateBy { it.id }

        val newAlarms = _activeAlarms.value.toMutableList()

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

                // Check snooze
                val snoozeUntil = snoozedAlarms[alarmKey]
                if (snoozeUntil != null && nowMillis >= snoozeUntil && nowMillis < startMillis) {
                    snoozedAlarms.remove(alarmKey)
                    if (newAlarms.none { it.key == alarmKey }) {
                        newAlarms.add(
                            ActiveAlarmItem(
                                key = alarmKey,
                                tuitionClass = c,
                                teacher = teacherMap[c.teacherId],
                                triggerTimeMillis = nowMillis,
                                classStartMillis = startMillis,
                                classEndMillis = endMillis
                            )
                        )
                    }
                }

                // If within reminder window and hasn't fired yet
                if (nowMillis in reminderMillis until startMillis && !firedAlarmsKeys.contains(alarmKey)) {
                    firedAlarmsKeys.add(alarmKey)
                    val teacher = teacherMap[c.teacherId]
                    NotificationHelper.showClassReminder(
                        context = getApplication(),
                        subject = c.subject,
                        startTime = TimeUtil.format12HourString(c.start),
                        teacherName = teacher?.name,
                        address = teacher?.address,
                        minutesUntilStart = c.remindMinutes
                    )

                    if (newAlarms.none { it.key == alarmKey }) {
                        newAlarms.add(
                            ActiveAlarmItem(
                                key = alarmKey,
                                tuitionClass = c,
                                teacher = teacher,
                                triggerTimeMillis = reminderMillis,
                                classStartMillis = startMillis,
                                classEndMillis = endMillis
                            )
                        )
                    }
                }
            }
        }

        // Clean up alarms whose class has ended
        newAlarms.removeAll { nowMillis >= it.classEndMillis }
        _activeAlarms.value = newAlarms
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
}
