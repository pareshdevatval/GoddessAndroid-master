package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.PracticeEquipment

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface PracticeEquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(practiceEuipment: PracticeEquipment)

    @Query("SELECT peEquipmentId from PracticeEquipment where pePracticeId = :practiceId")
    fun getEquipmentId(practiceId: Int) : Int
}