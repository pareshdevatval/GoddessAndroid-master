package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.ForgetPasswordResponse
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.MyProfileResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class OrderSuccessViewModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null


    private val myProfileResponse: MutableLiveData<MyProfileResponse> by lazy {
        MutableLiveData<MyProfileResponse>()
    }

    fun getMyProfileResponse(): LiveData<MyProfileResponse> {
        return myProfileResponse
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

    private fun handleResponse(response: MyProfileResponse) {
        myProfileResponse.value = response
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