package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.krystal.goddesslifestyle.data.response.RecipeOfTheMonthImage

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface RecipeImageOfTheMonthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipeOfTheMonthImage: RecipeOfTheMonthImage)

    /*@Query("SELECT calendarTitle from CalendarsData where calendarThemeId = :themeId")
    fun getDayLabels(themeId: Int) : List<String?>?

    @Query("SELECT calendarId from CalendarsData where calendarDay = :date")
    fun getCalenderDayId(date: Int) : Int*/

}