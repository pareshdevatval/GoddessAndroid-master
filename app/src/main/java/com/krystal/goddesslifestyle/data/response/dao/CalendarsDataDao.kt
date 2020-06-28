package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.CalendarsData

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface CalendarsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calData: CalendarsData?)

    @Query("SELECT calendarTitle from CalendarsData where calendarThemeId = :themeId")
    fun getDayLabels(themeId: Int) : List<String?>?

    @Query("SELECT calendarId from CalendarsData where calendarDay = :date")
    fun getCalenderDayId(date: Int) : Int

}