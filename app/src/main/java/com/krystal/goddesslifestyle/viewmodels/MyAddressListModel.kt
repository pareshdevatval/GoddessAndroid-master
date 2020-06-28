package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class MyAddressListModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val myAddressListResponse: MutableLiveData<MyAddressListResponse> by lazy {
        MutableLiveData<MyAddressListResponse>()
    }

    fun getMyAddressListResponse(): LiveData<MyAddressListResponse> {
        return myAddressListResponse
    }

    private val deleteResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getDeleteAddressResponse(): LiveData<BaseResponse> {
        return deleteResponse
    }


    fun callMyAddressListApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiMyAddressList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun callDeleteApi(paras:HashMap<String,Any?>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiDeleteMyAddress(paras)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleDeleteResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: MyAddressListResponse) {
        myAddressListResponse.value = response
    }
    private fun handleDeleteResponse(response: BaseResponse) {
        deleteResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}