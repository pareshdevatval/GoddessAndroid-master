package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.GoddessCommunityCountResponse
import com.krystal.goddesslifestyle.data.response.LikeUnlikeResponse
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 30/3/20
 */
class GoddessCommunityViewModel(application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val goddessCommunityCountResponse: MutableLiveData<GoddessCommunityCountResponse> by lazy {
        MutableLiveData<GoddessCommunityCountResponse>()
    }

    fun getGoddessCommunityCountResponse(): LiveData<GoddessCommunityCountResponse> {
        return goddessCommunityCountResponse
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    fun callGetGoddessCommunityCount(showProgress: Boolean = true){
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetGoddessCommunityCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: GoddessCommunityCountResponse) {
        goddessCommunityCountResponse.value = response
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
}