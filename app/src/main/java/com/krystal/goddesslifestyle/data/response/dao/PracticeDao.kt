package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.Practice

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface PracticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(practice: Practice)

    @Query("SELECT * from Practice where practiceId = :practiceId")
    fun getPractice(practiceId: Int) : Practice?
}