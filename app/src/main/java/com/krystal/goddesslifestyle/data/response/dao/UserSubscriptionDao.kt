package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.TodaysRecipe
import com.krystal.goddesslifestyle.data.response.UserSubscription

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface UserSubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userSubscription: UserSubscription)

    @Query("SELECT * from UserSubscription where usUserId = :userId AND usSubscriptionExpired = :isExpired")
    fun getUserSubscriptionData(userId: Int, isExpired: Boolean = false) : List<UserSubscription>

    @Query("SELECT * from UserSubscription where usUserId = :userId")
    fun getExpiredSubscriptionData(userId: Int) : List<UserSubscription>
}