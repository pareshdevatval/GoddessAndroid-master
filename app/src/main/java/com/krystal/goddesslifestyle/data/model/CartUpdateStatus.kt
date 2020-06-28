package com.krystal.goddesslifestyle.data.model

import com.krystal.goddesslifestyle.data.db.model.Cart
import java.text.FieldPosition

/**
 * Created by Bhargav Thanki on 10 April,2020.
 */
data class CartUpdateStatus(val success: Boolean,
                            val updateType: String,
                            val cartItem: Cart,
                            val position: Int) {
}