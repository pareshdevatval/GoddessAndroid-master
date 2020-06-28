package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.ForgetPasswordResponse
import com.krystal.goddesslifestyle.data.response.MyProfileResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class MyProfileModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null


    private lateinit var appDatabase: AppDatabase


    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
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

    private val myProfileResponse: MutableLiveData<MyProfileResponse> by lazy {
        MutableLiveData<MyProfileResponse>()
    }

    fun getMyProfileResponse(): LiveData<MyProfileResponse> {
        return myProfileResponse
    }
    private val logoutResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getLogoutResponse(): LiveData<BaseResponse> {
        return logoutResponse
    }
    private fun handleResponse(response: MyProfileResponse) {
        myProfileResponse.value = response
    }
    private fun handleLogoutResponse(response: BaseResponse) {
        logoutResponse.value = response
    }
    private val shareApiResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getShareApiResponse(): LiveData<BaseResponse> {
        return shareApiResponse
    }


    fun callMyProfile(id:Int) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetMyProfile(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }
    fun callLogOut() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiLogOut()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleLogoutResponse, this::handleError)
        } else {
            onInternetError()
        }
    }


    fun callShareApi(params: HashMap<String, String>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiAddUserPoints(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /*onApiStart()*/ }
                .doOnTerminate { /*onApiFinish()*/ }
                .subscribe(this::handleShareResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleShareResponse(response: BaseResponse) {
        this.shareApiResponse.value = response
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

    fun clearCartData() {
        appDatabase.cartDao().nukeTable()
        appDatabase.cartProductDao().nukeTable()
        appDatabase.cartAmountDao().nukeTable()
    }
}