package com.krystal.goddesslifestyle.data.db.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.response.Journal

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Cart)

    @Query("UPDATE Cart SET quantity = :newQty where productId = :productId")
    fun updateQuantity(productId: Int, newQty : Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCartItem(cartIem: Cart)

    @Query("SELECT * from Cart where productId = :productId")
    fun getCartItem(productId: Int): Cart?

    @Query("SELECT * from Cart")
    fun getCartData() : List<Cart>

    @Delete
    fun deleteCartItem(cartIem: Cart)

    @Query("DELETE FROM Cart")
    fun nukeTable()
}