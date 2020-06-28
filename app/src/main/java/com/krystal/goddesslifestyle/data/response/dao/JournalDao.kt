package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.Journal

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(journal: Journal)

    @Query("SELECT * from Journal where jpId = :journalId")
    fun getJournal(journalId: Int) : Journal?
}