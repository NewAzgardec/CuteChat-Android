package com.example.kurs.current

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kurs.common.Converters

@Database(entities = [CurrentUser::class], version = 1)
@TypeConverters(Converters::class)
abstract class DBUser : RoomDatabase() {
    abstract val userDao: UserDao

    companion object {
        private var INSTANCE: DBUser? = null
        fun getInstance(context: Context): DBUser {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    DBUser::class.java,
                    "roomdb"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE as DBUser
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
