package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by Bhargav Thanki on 20 February,2020.
 */
class TodaysMealViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    /*[START] code to call share points API*/
    private val shareApiResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getShareApiResponse(): LiveData<BaseResponse> {
        return shareApiResponse
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
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
    }
    /*[END] code to call share points API*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

}