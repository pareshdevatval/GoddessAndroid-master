package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.model.CartProduct
import com.krystal.goddesslifestyle.data.response.OrderListResponse
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.data.response.ShopResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class OrderListModel(application: Application) : BaseViewModel(application) {

    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val ordderResponse: MutableLiveData<OrderListResponse> by lazy {
        MutableLiveData<OrderListResponse>()
    }

    fun getOrderListResponse(): LiveData<OrderListResponse> {
        return ordderResponse
    }


    fun callOrderListApi(page:Int=0,showProgress: Boolean = true,timeFilter:Int=0,orderFilter:Int=0) {
        if (AppUtils.hasInternet(getApplication())) {
            val params:HashMap<String,Any> = HashMap()
            params[ApiParam.TIME_FILTER]=timeFilter
            params[ApiParam.ORDER_FILTER]=orderFilter
            params[ApiParam.PAGE_NO]=page
            params[ApiParam.KEY_LIMIT]=AppConstants.ITEMS_LIMIT
            subscription = apiServiceObj
                .apiGetOrderList(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: OrderListResponse) {
        ordderResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out))
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    fun clearCartData() {
        appDatabase.cartDao().nukeTable()
        appDatabase.cartProductDao().nukeTable()
        appDatabase.cartAmountDao().nukeTable()
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    /*[START] Buy It Again code*/
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

    private val addToCartResponse: MutableLiveData<CartProduct> by lazy {
        MutableLiveData<CartProduct>()
    }

    fun getAddToCartAcknowledgement(): LiveData<CartProduct> {
        return addToCartResponse
    }

    fun addProductToCart(strQuantity: String, product: Product?, position: Int) {
        if (strQuantity.isNotBlank() && strQuantity.toInt() != 0) {
            val quantity = strQuantity.toInt()
            if (product?.productPrice == null) {
                val cartProduct = CartProduct(false, product, position)
                addToCartResponse.value = cartProduct
            } else {
                appDatabase.cartProductDao().insert(product)
                val cart = Cart()
                cart.productId = product.productId
                cart.quantity = quantity
                cart.amount = product.productPrice!!.toDouble()
                appDatabase.cartDao().insert(cart)
                //addToCartResponse.value = true

                updateCartAmountTable(product, position)

            }
        } else {
            val cartProduct = CartProduct(false, product, position)
            addToCartResponse.value = cartProduct
        }
    }

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
    /*[END] Buy It Again code*/
}