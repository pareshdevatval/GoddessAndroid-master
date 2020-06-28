package com.krystal.goddesslifestyle.data.model

import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.response.Product
import java.text.FieldPosition

/**
 * Created by Bhargav Thanki on 10 April,2020.
 */
data class CartProduct(val success: Boolean,
                       val product: Product?,
                       val position: Int) {
}