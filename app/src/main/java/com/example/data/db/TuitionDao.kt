package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    @Query("SELECT * FROM tuition_classes ORDER BY start ASC")
    fun getAllClasses(): Flow<List<TuitionClass>>

    @Query("SELECT * FROM tuition_classes WHERE day = :day ORDER BY start ASC")
    fun getClassesForDay(day: Int): Flow<List<TuitionClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(tuitionClass: TuitionClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<TuitionClass>)

    @Update
    suspend fun updateClass(tuitionClass: TuitionClass)

    @Delete
    suspend fun deleteClass(tuitionClass: TuitionClass)

    @Query("DELETE FROM tuition_classes WHERE id = :id")
    suspend fun deleteClassById(id: String)

    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("UPDATE tuition_classes SET teacherId = NULL WHERE teacherId = :teacherId")
    suspend fun clearTeacherFromClasses(teacherId: String)

    @Query("DELETE FROM tuition_classes")
    suspend fun clearAllClasses()

    @Query("DELETE FROM teachers")
    suspend fun clearAllTeachers()
}
