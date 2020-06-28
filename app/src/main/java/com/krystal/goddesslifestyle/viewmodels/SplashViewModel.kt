package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.AppSettingsResponse
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class SplashViewModel(application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null
    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val userSubscriptionResponse: MutableLiveData<UserSubscriptionResponse> by lazy {
        MutableLiveData<UserSubscriptionResponse>()
    }

    fun getUserSubscriptionResponse(): LiveData<UserSubscriptionResponse> {
        return userSubscriptionResponse
    }

    fun callGetUserSubscriptionApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetUserSubscriptions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /*onApiStart()*/ }
                .doOnTerminate { /*onApiFinish()*/ }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: UserSubscriptionResponse) {
        userSubscriptionResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out))
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    private val appSettingsResponse: MutableLiveData<AppSettingsResponse> by lazy {
        MutableLiveData<AppSettingsResponse>()
    }

    fun getAppSettingsResponse(): LiveData<AppSettingsResponse> {
        return appSettingsResponse
    }

    fun callAppSettingsApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiAppSettings()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /*onApiStart()*/ }
                .doOnTerminate { /*onApiFinish()*/ }
                .subscribe(this::handleSettingsResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleSettingsResponse(response: AppSettingsResponse) {
        appSettingsResponse.value = response
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}