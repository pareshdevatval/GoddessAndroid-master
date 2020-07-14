package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.CalendarsData
import com.krystal.goddesslifestyle.data.response.Recipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface CalendarsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calData: CalendarsData?)

    @Query("SELECT calendarTitle from CalendarsData where calendarThemeId = :themeId order by calendarDay")
    fun getDayLabels(themeId: Int): List<String?>?

    @Query("SELECT calendarDay from CalendarsData where calendarThemeId = :themeId order by calendarDay")
    fun getDayNo(themeId: Int): List<Int?>?

    @Query("SELECT calendarId from CalendarsData where calendarDay = :date")
    fun getCalenderDayId(date: Int): Int

    @Query("SELECT * from CalendarsData where calendarId = :calId")
    fun getCalenderDataFromId(calId: Int): CalendarsData

    @Query("DELETE FROM CalendarsData")
    fun clearData()

    @Delete
    fun deleteCalendarsData(calData: CalendarsData?)
}