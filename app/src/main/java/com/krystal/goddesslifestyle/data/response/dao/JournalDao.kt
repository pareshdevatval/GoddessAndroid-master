package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.Journal
import com.krystal.goddesslifestyle.data.response.Recipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(journal: Journal)

    @Query("SELECT * from Journal where jpId = :journalId")
    fun getJournal(journalId: Int) : Journal?

    @Delete
    fun deleteJournal(journal: Journal)
}