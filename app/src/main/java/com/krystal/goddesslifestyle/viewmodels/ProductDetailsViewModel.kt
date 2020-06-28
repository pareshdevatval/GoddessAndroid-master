package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by imobdev on 21/2/20
 */
class ProductDetailsViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var appDatabase: AppDatabase

    /*RxJava Subscription object for api calling*/
    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val addToCartResponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getAddToCartAcknowledgement(): LiveData<Boolean> {
        return addToCartResponse
    }

    /*Adding the product to cart*/
    /*Accepts
    * @strQuantity -> quantity of the item
    * @product -> Product object
    * */
    fun addProductToCart(strQuantity: String, product: Product?) {
        if (strQuantity.isNotBlank() && strQuantity.toInt() != 0) {
            val quantity = strQuantity.toInt()
                if (product?.productPrice == null) {
                    // If price is null, then we will not add the product
                    // and notify failure
                    addToCartResponse.value = false
                } else {
                    // inseting product to the cart product table
                    appDatabase.cartProductDao().insert(product)
                    // We will add cart data into cart table
                    val cart = Cart()
                    cart.productId = product.productId
                    cart.quantity = quantity
                    cart.amount = product.productPrice!!.toDouble()
                    appDatabase.cartDao().insert(cart)
                    updateCartAmountTable()
                }
        } else {
            addToCartResponse.value = false
        }
    }

    // Updating cart amount table
    private fun updateCartAmountTable() {
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
        addToCartResponse.value = true
    }

    override fun onCleared() {
        super.onCleared()
    }
}