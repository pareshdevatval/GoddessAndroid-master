package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class SubscriptionViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }


    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val verifySubscriptionResponse: MutableLiveData<UserSubscriptionResponse> by lazy {
        MutableLiveData<UserSubscriptionResponse>()
    }

    fun getVerifyResponse(): LiveData<UserSubscriptionResponse> {
        return verifySubscriptionResponse
    }

    /*To Check if user already owns a subscription or not*/
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

    fun callVerifySubscriptionApi(showProgress: Boolean = false, params: HashMap<String, String>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiVerifySubscription(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if(showProgress) onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: UserSubscriptionResponse) {
        verifySubscriptionResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out))
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}