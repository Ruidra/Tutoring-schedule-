package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.TuitionDao
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TuitionRepository(
    private val dao: TuitionDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tuition_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode = _themeMode.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDERS_ENABLED, true))
    val remindersEnabled = _remindersEnabled.asStateFlow()

    val allClasses: Flow<List<TuitionClass>> = dao.getAllClasses()
    val allTeachers: Flow<List<Teacher>> = dao.getAllTeachers()

    suspend fun saveClass(tuitionClass: TuitionClass) {
        dao.insertClass(tuitionClass)
    }

    suspend fun deleteClass(tuitionClass: TuitionClass) {
        dao.deleteClass(tuitionClass)
    }

    suspend fun deleteClassById(id: String) {
        dao.deleteClassById(id)
    }

    suspend fun saveTeacher(teacher: Teacher) {
        dao.insertTeacher(teacher)
    }

    suspend fun deleteTeacher(teacher: Teacher) {
        dao.clearTeacherFromClasses(teacher.id)
        dao.deleteTeacher(teacher)
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
        _remindersEnabled.value = enabled
    }

    suspend fun seedSampleDataIfEmpty(currentClassesCount: Int) {
        if (currentClassesCount > 0) return
        seedSampleData()
    }

    suspend fun seedSampleData() {
        dao.clearAllClasses()
        dao.clearAllTeachers()

        val teacher1 = Teacher(
            id = UUID.randomUUID().toString(),
            name = "M. R. Rahman Sir",
            teaches = "Physics 1st & 2nd Paper",
            address = "House 14, Road 5, Dhanmondi, Dhaka",
            phone = "01711234567"
        )
        val teacher2 = Teacher(
            id = UUID.randomUUID().toString(),
            name = "Prof. Anisul Haque",
            teaches = "Chemistry & Organic Chemistry",
            address = "Flat 4B, Green Road, Farmgate",
            phone = "01819876543"
        )
        val teacher3 = Teacher(
            id = UUID.randomUUID().toString(),
            name = "Tanvir Ahmed Sir",
            teaches = "Higher Mathematics & Calculus",
            address = "Sector 4, Uttara, Dhaka",
            phone = "01912348901"
        )

        dao.insertTeachers(listOf(teacher1, teacher2, teacher3))

        val sampleClasses = listOf(
            // Saturday (day = 6)
            TuitionClass(
                day = 6,
                start = "09:00",
                end = "10:30",
                subject = "Physics 1st Paper",
                teacherId = teacher1.id,
                note = "Vector analysis chapter test today",
                remindMinutes = 30
            ),
            TuitionClass(
                day = 6,
                start = "16:00",
                end = "17:30",
                subject = "Chemistry 2nd Paper",
                teacherId = teacher2.id,
                note = "Bring organic chemistry reaction chart",
                remindMinutes = 30
            ),
            // Sunday (day = 0)
            TuitionClass(
                day = 0,
                start = "17:00",
                end = "18:30",
                subject = "Higher Math",
                teacherId = teacher3.id,
                note = "Calculus integration problem set #4",
                remindMinutes = 30
            ),
            // Monday (day = 1)
            TuitionClass(
                day = 1,
                start = "16:00",
                end = "17:30",
                subject = "Physics 2nd Paper",
                teacherId = teacher1.id,
                note = "Thermodynamics heat engine formulas",
                remindMinutes = 30
            ),
            TuitionClass(
                day = 1,
                start = "18:00",
                end = "19:30",
                subject = "Biology",
                teacherId = null,
                note = "Genetics & DNA replication notes",
                remindMinutes = 30
            ),
            // Tuesday (day = 2)
            TuitionClass(
                day = 2,
                start = "16:30",
                end = "18:00",
                subject = "Higher Math",
                teacherId = teacher3.id,
                note = "Coordinate geometry ellipse practice",
                remindMinutes = 30
            ),
            // Wednesday (day = 3)
            TuitionClass(
                day = 3,
                start = "15:30",
                end = "17:00",
                subject = "Chemistry 1st Paper",
                teacherId = teacher2.id,
                note = "Chemical equilibrium numerical problems",
                remindMinutes = 30
            ),
            TuitionClass(
                day = 3,
                start = "17:30",
                end = "19:00",
                subject = "ICT",
                teacherId = null,
                note = "HTML, CSS & logic gates chapter",
                remindMinutes = 30
            ),
            // Thursday (day = 4)
            TuitionClass(
                day = 4,
                start = "17:00",
                end = "18:30",
                subject = "English 2nd Paper",
                teacherId = null,
                note = "Transformation of sentences & modifiers",
                remindMinutes = 30
            )
        )

        dao.insertClasses(sampleClasses)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_REMINDERS_ENABLED = "key_reminders_enabled"
    }
}
