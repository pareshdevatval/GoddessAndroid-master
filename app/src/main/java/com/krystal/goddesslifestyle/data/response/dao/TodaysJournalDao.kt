package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.TodaysJournal

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface TodaysJournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg todaysJournals: TodaysJournal)

    @Query("SELECT cjpJpId from TodaysJournal where cjpCalendarId = :date")
    fun getTodayJournalId(date: Int) : Int
}