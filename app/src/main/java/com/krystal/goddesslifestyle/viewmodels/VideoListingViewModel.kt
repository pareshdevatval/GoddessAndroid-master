package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.VideoCategoriesResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class VideoListingViewModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val videoCategoriesResponse: MutableLiveData<VideoCategoriesResponse> by lazy {
        MutableLiveData<VideoCategoriesResponse>()
    }

    fun getVideoCategoriesResponse(): LiveData<VideoCategoriesResponse> {
        return videoCategoriesResponse
    }

    fun callVideoCategoriesApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetVideoCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: VideoCategoriesResponse) {
        videoCategoriesResponse.value = response
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