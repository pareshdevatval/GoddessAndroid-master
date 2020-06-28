package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.VideosListResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class VideoDetailsViewModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val videosResponse: MutableLiveData<VideosListResponse> by lazy {
        MutableLiveData<VideosListResponse>()
    }

    fun getVideosResponse(): LiveData<VideosListResponse> {
        return videosResponse
    }

    fun callVideosApi(params: HashMap<String, String>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetVideos(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: VideosListResponse) {
        videosResponse.value = response
    }


    private fun handleError(error: Throwable) {
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), "Socket time-out")
        } else {
            apiErrorMessage.value = error.localizedMessage
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}