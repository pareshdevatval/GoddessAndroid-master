package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.response.Journal
import com.krystal.goddesslifestyle.data.response.Product

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface CartProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product)

    @Query("SELECT * from CartProduct")
    fun getCartProducts() : List<Product>

    @Query("SELECT * from CartProduct where productId = :productId")
    fun getProductById(productId: Int) : Product?

    @Delete
    fun deleteProduct(product: Product)

    @Query("DELETE FROM CartProduct")
    fun nukeTable()
}