package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.Equipment

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(quipment: Equipment)

    @Query("SELECT * from Equipment where equipmentId = :equipmentId")
    fun getEquipment(equipmentId: Int) : Equipment

}