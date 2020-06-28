package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.TodaysRecipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface TodaysRecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(todaysRecipe: TodaysRecipe)

    @Query("SELECT crRecipeId from TodaysRecipe where crCalendarId = :date")
    fun getTodayRecipeId(date: Int) : List<Int>
}