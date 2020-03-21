package com.example.kurs.current

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: CurrentUser)

    @Delete
    fun delete(user: CurrentUser)

    @Query("SELECT * FROM user")
    fun getAll(): List<CurrentUser>

    @Update
    fun update(user: CurrentUser)
}