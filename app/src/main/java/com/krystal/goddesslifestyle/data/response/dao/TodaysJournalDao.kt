package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.TodaysJournal
import com.krystal.goddesslifestyle.data.response.TodaysRecipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface TodaysJournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg todaysJournals: TodaysJournal)

    @Query("SELECT cjpJpId from TodaysJournal where cjpCalendarId = :date")
    fun getTodayJournalId(date: Int) : Int

    @Query("DELETE FROM TodaysJournal WHERE cjpCalendarId = :date")
    fun deleteJournal(date: Int) : Int

}