package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class HowToEarnViewModel(application: Application) : BaseViewModel(application)  {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val howToEarnPointResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getHowToEarnResponse(): LiveData<BaseResponse> {
        return howToEarnPointResponse
    }


    fun callHowToEarnPointApi(id:String,points:String,type:String,showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, String> = HashMap()
            params[ApiParam.ACTIVITY_ID] = id
            params[ApiParam.ACTIVITY_POINTS] = points
            params[ApiParam.ACTIVITY_TYPE] = type
            subscription = apiServiceObj
                .apiAddUserPoints(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: BaseResponse) {
        howToEarnPointResponse.value = response
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