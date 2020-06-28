package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.ThemeMaster

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface ThemeMasterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(themeMaster: ThemeMaster)

    @Query("SELECT * from ThemeMaster where taYear = :currentYear AND taMonth = :currentMonth")
    fun getThemeMaster(currentYear: Int, currentMonth: Int) : ThemeMaster?

}