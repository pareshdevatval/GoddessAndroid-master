package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.model.CartProduct
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.data.response.ShopResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class ShopViewModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val shopResponse: MutableLiveData<ShopResponse> by lazy {
        MutableLiveData<ShopResponse>()
    }

    fun getShopResponse(): LiveData<ShopResponse> {
        return shopResponse
    }

    fun callShopApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiShop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: ShopResponse) {
        shopResponse.value = response
    }


    private fun handleError(error: Throwable) {
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), "Socket time-out")
        } else {
            apiErrorMessage.value = error.localizedMessage
        }
    }

    /*[START] Add Product to Cart*/
    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val addToCartResponse: MutableLiveData<CartProduct> by lazy {
        MutableLiveData<CartProduct>()
    }

    fun getAddToCartAcknowledgement(): LiveData<CartProduct> {
        return addToCartResponse
    }

    /*Adding the product to cart*/
    /*Accepts
    * @strQuantity -> quantity of the item
    * @product -> Product object
    * @position -> recyclerview position to update the list after success
    * */
    fun addProductToCart(strQuantity: String, product: Product?, position: Int) {
        // add if quantity is >0
        if (strQuantity.isNotBlank() && strQuantity.toInt() != 0) {
            val quantity = strQuantity.toInt()
            if (product?.productPrice == null) {
                // If price is null, then we will not add the product
                // and notify failure
                val cartProduct = CartProduct(false, product, position)
                addToCartResponse.value = cartProduct
            } else {
                // inseting product to the cart product table
                appDatabase.cartProductDao().insert(product)
                // We will add cart data into cart table
                val cart = Cart()
                cart.productId = product.productId
                cart.quantity = quantity
                cart.amount = product.productPrice!!.toDouble()
                appDatabase.cartDao().insert(cart)

                // Update to total amount table adter product is added to cart
                updateCartAmountTable(product, position)

            }
        } else {
            val cartProduct = CartProduct(false, product, position)
            addToCartResponse.value = cartProduct
        }
    }

    // Updating cart amount table
    private fun updateCartAmountTable(product: Product, position: Int) {
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

        val cartProduct = CartProduct(true, product, position)
        addToCartResponse.value = cartProduct
    }
    /*[END] Add Product to Cart*/

    /*Check if item already exists in the cart or not
    * if Yes then update the entry otherwise add the entry*/
    fun isItemAlreadyInCart(product: Product): Boolean {
        val cartItems = appDatabase.cartDao().getCartData()
        for(cartItem in cartItems) {
            if(cartItem.productId!! == product.productId) {
                return true
            }
        }
        return false;
    }

    /*[START] Remove an Item from Cart*/

    private val deleteItemFromCartResponse: MutableLiveData<CartProduct> by lazy {
        MutableLiveData<CartProduct>()
    }

    fun getDeleteCartItemAcknowledgement(): LiveData<CartProduct> {
        return deleteItemFromCartResponse
    }

    // Removing an item from cart
    fun deleteItemFromCart(product: Product, position: Int) {
        val productId = product.productId

        productId?.let {pId ->
            // removing from cart table
            val cartItem = appDatabase.cartDao().getCartItem(pId)
            cartItem?.let {
                appDatabase.cartDao().deleteCartItem(it)
            }
            val prdt = appDatabase.cartProductDao().getProductById(pId)
            // removing from cart product table
            prdt?.let {
                appDatabase.cartProductDao().deleteProduct(it)
            }

            val cartProduct = CartProduct(true, product, position)
            deleteItemFromCartResponse.value = cartProduct
        }
    }
    /*[END] Remove an Item from Cart*/


    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}