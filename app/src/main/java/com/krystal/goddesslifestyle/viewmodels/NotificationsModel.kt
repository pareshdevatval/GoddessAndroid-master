package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.GetNotificationListResponse
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class NotificationsModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val getNotificationResponse: MutableLiveData<GetNotificationListResponse> by lazy {
        MutableLiveData<GetNotificationListResponse>()
    }

    fun getGetNotificationListResponse(): LiveData<GetNotificationListResponse> {
        return getNotificationResponse
    }

       private val removeNotification: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getRemoveNotificationResponse(): LiveData<BaseResponse> {
        return removeNotification
    }

    private val clearAllNotification: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getClearNotificationResponse(): LiveData<BaseResponse> {
        return clearAllNotification
    }


    fun callNotificationListApi(pageNo: Int = 1,showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.KEY_PAGE] = pageNo.toString()
            params[ApiParam.KEY_LIMIT] = ApiContants.LIMIT
            subscription = apiServiceObj
                .apiGetNotification(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleNotificationResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleNotificationResponse(response: GetNotificationListResponse) {
        getNotificationResponse.value = response
    }

    fun callRemoveNotificationApi(nId: Int,showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.N_ID] = nId
            params[ApiParam.DEVICE_TYPE] = "android"
            subscription = apiServiceObj
                .apiRemoveNotification(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleRemoveResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

     fun callClearAllNotificationApi() {
            if (AppUtils.hasInternet(getApplication())) {
                val params: HashMap<String, Any> = HashMap()
                params[ApiParam.DEVICE_TYPE] = "android"
                subscription = apiServiceObj
                    .apiClearNotification(params)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe {onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleClearAllResponse, this::handleError)
            } else {
                onInternetError()
            }
        }



    private fun handleRemoveResponse(response: BaseResponse) {
        removeNotification.value = response
    }

    private fun handleClearAllResponse(response: BaseResponse) {
        clearAllNotification.value = response
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