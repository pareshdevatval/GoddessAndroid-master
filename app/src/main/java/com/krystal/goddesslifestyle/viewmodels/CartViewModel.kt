package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.model.CartUpdateStatus
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by imobdev on 21/2/20
 */
class CartViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var appDatabase: AppDatabase

    /*RxJava Subscription object for api calling*/
    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val updateCartResponse: MutableLiveData<CartUpdateStatus> by lazy {
        MutableLiveData<CartUpdateStatus>()
    }

    fun getUpdateCartAcknowledgement(): LiveData<CartUpdateStatus> {
        return updateCartResponse
    }

    // change the quantity of product
    fun changeQuantity(cartItem: Cart, increseDecrease: Int, listPosition: Int) {
        var quantity = cartItem.quantity
        if (increseDecrease == AppConstants.INCREASE_QUANTITY) {
            quantity += 1
        } else if (increseDecrease == AppConstants.DECREASE_QUANTITY) {
            if (quantity != 1) {
                quantity -= 1
            }
        }
        cartItem.quantity = quantity
        cartItem.productId?.let {
            appDatabase.cartDao().updateCartItem(cartItem)
            updateCartAmountTable(AppConstants.UPDATE_TYPE_CHANGE_ITEM, cartItem, listPosition)
        }
    }

    // Delete an item
    fun deleteItemFromCart(cartItem: Cart, position: Int) {
        val productId = cartItem.productId
        appDatabase.cartDao().deleteCartItem(cartItem)
        productId?.let {
            val product = appDatabase.cartProductDao().getProductById(it)
            product?.let {
                appDatabase.cartProductDao().deleteProduct(it)
            }
        }
        updateCartAmountTable(AppConstants.UPDATE_TYPE_DELETE_ITEM, cartItem, position)
    }

    private fun updateCartAmountTable(updateType: String, cartItem: Cart, position: Int) {
        val cartData = appDatabase.cartDao().getCartData()
        var subAmount = 0.0
        for (cart in cartData) {
            cart.amount?.let {
                val productTotal = it * cart.quantity
                subAmount += productTotal
            }
        }

        val deliveryCharges = 0.0

        val cartAmount = CartAmount()
        cartAmount.subTotal = subAmount
        cartAmount.deliveryChargees = deliveryCharges
        cartAmount.totalAmount = subAmount + deliveryCharges
        cartAmount.useRedeemPoint = false
        cartAmount.redeemPoints = 0.0
        cartAmount.newTotalAmount = subAmount + deliveryCharges

        appDatabase.cartAmountDao().delete()
        appDatabase.cartAmountDao().insert(cartAmount)

        val cartUpdateStatus = CartUpdateStatus(true, updateType, cartItem, position)
        updateCartResponse.value = cartUpdateStatus
    }

    override fun onCleared() {
        super.onCleared()
    }
}