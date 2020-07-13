package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.TodaysJournal
import com.krystal.goddesslifestyle.data.response.TodaysPractic

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface TodaysPracticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(todaysPractice: TodaysPractic)

    @Query("SELECT cpPracticeId from TodaysPractic where cpCalendarId = :date")
    fun getTodayPracticeId(date: Int) : Int

    @Delete
    fun deletePractice(todaysPractice: TodaysPractic)
}