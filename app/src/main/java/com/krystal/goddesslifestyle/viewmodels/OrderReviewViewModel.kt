package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.model.CartUpdateStatus
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class OrderReviewViewModel(application: Application) : BaseViewModel(application) {

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

    fun changeQuantity(cartItem: Cart, increseDecrease: Int, listPosition: Int,
                       selectedAddress: MyAddressListResponse.Result?) {
        var quantity = cartItem.quantity
        if(increseDecrease == AppConstants.INCREASE_QUANTITY) {
            quantity += 1
        } else if(increseDecrease == AppConstants.DECREASE_QUANTITY) {
            if(quantity != 1) {
                quantity -= 1
            }
        }
        cartItem.quantity = quantity
        cartItem.productId?.let {
            appDatabase.cartDao().updateCartItem(cartItem)
            updateCartAmountTable(AppConstants.UPDATE_TYPE_CHANGE_ITEM, cartItem, listPosition,
                selectedAddress)
        }
    }

    fun deleteItemFromCart(cartItem: Cart, position: Int, selectedAddress: MyAddressListResponse.Result?) {
        val productId = cartItem.productId
        appDatabase.cartDao().deleteCartItem(cartItem)
        productId?.let {
            val product = appDatabase.cartProductDao().getProductById(it)
            product?.let {
                appDatabase.cartProductDao().deleteProduct(it)
            }
        }
        //(binding.recyclerView.adapter as CartAdapter).removeItem(position)

        updateCartAmountTable(AppConstants.UPDATE_TYPE_DELETE_ITEM, cartItem, position, selectedAddress)
    }

    private fun updateCartAmountTable(updateType: String, cartItem: Cart, position: Int,
                                      selectedAddress: MyAddressListResponse.Result?) {
        val cartData = appDatabase.cartDao().getCartData()

        val cartAmountDb = appDatabase.cartAmountDao().getCartData()

        var subAmount = 0.0
        for(cart in cartData) {
            cart.amount?.let {
                val productTotal = it * cart.quantity
                subAmount += productTotal
            }
        }

        val deliveryCharges = cartAmountDb?.deliveryChargees!!
        /*var deliveryCharges = 0.0
        if(selectedAddress == null || selectedAddress.uaCountry == null) {
            deliveryCharges = cartAmountDb?.deliveryChargees!!
        } else {
            deliveryCharges = AppUtils.getDeliveryCharges(
                getApplication(), subAmount,
                selectedAddress.uaCountry
            )
        }*/

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

    /*[START] Place order API*/
    private var subscription: Disposable? = null





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

    /*[END] Place order API*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    /*[START] address code*/

    private val myAddressListResponse: MutableLiveData<MyAddressListResponse> by lazy {
        MutableLiveData<MyAddressListResponse>()
    }

    fun getMyAddressListResponse(): LiveData<MyAddressListResponse> {
        return myAddressListResponse
    }

    fun callMyAddressListApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiMyAddressList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: MyAddressListResponse) {
        myAddressListResponse.value = response
    }
    /*[END] address code*/
}