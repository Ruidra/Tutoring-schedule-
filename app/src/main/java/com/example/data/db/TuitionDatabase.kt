package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass

@Database(
    entities = [TuitionClass::class, Teacher::class],
    version = 1,
    exportSchema = false
)
abstract class TuitionDatabase : RoomDatabase() {
    abstract fun tuitionDao(): TuitionDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDatabase? = null

        fun getDatabase(context: Context): TuitionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDatabase::class.java,
                    "tuition_routine_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
