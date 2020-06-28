package com.krystal.goddesslifestyle.data.db.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.response.Journal

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface CartAmountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cartAmount: CartAmount)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(cartAmount: CartAmount)

    @Query("SELECT * from CartAmount")
    fun getCartData() : CartAmount?

    @Query("DELETE from CartAmount")
    fun delete()

    @Query("DELETE FROM CartAmount")
    fun nukeTable()
}