package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.SettingResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class SettingModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val settingResponse: MutableLiveData<SettingResponse> by lazy {
        MutableLiveData<SettingResponse>()
    }

    fun getSettingResponse(): LiveData<SettingResponse> {
        return settingResponse
    }
    private val cancleResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getCancelResponse(): LiveData<BaseResponse> {
        return cancleResponse
    }
    private lateinit var appDatabase: AppDatabase


    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    fun callSettingApi(practice: Boolean, community: Boolean, shopping: Boolean) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.ENABLE_PRACTICE_NOTI] = if (practice) {
                1
            } else {
                2
            }
            params[ApiParam.ENABLE_COMMUNITY_NOTI] = if (community) {
                1
            } else {
                2
            }
            params[ApiParam.ENABLE_SHOPPING_NOTI] = if (shopping) {
                1
            } else {
                2
            }
            subscription = apiServiceObj
                .apiSettingNotification(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }
    fun callCancelAccountApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiCencelAccount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleCancelAccountResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: SettingResponse) {
        settingResponse.value = response
    }
     private fun handleCancelAccountResponse(response: BaseResponse) {
         cancleResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
    fun clearCartData() {
        appDatabase.cartDao().nukeTable()
        appDatabase.cartProductDao().nukeTable()
        appDatabase.cartAmountDao().nukeTable()
    }

    fun getUserSubscription() : UserSubscription? {
        val user = prefsObj.userDataModel
        user?.let {
            it.result?.let {userData ->
                val userSubscriptions = appDatabase.userSubscriptionDao().getUserSubscriptionData(userData.uId!!)
                if(userSubscriptions.isEmpty()) {
                    return null
                } else {
                    return userSubscriptions[0]
                }
            }
        }
        return null
    }
}