package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.Recipe
import com.krystal.goddesslifestyle.data.response.TodaysRecipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface TodaysRecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(todaysRecipe: TodaysRecipe)

    @Query("SELECT crRecipeId from TodaysRecipe where crCalendarId = :date")
    fun getTodayRecipeId(date: Int): List<Int>

    @Query("DELETE FROM TodaysRecipe WHERE crCalendarId = :date")
    fun deleteRecipes(date: Int)
}