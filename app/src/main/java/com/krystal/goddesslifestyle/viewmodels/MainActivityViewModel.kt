package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.georeminder.utils.validator.ValidationErrorModel
import com.georeminder.utils.validator.Validator
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class MainActivityViewModel(application: Application): BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null


    private val baseResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun getBaseResponse(): LiveData<BaseResponse> {
        return baseResponse
    }

    /*[START] Below method is just for demostration of error messages live data*/
    fun onChangePassword(currentPwd: String, newPwd: String, confirmPwd: String) {
        Validator.validatePassword(currentPwd, R.string.blank_password)?.let {
            validationError.value = it
            return
        }
        Validator.validateConfirmPassword(newPwd, confirmPwd)?.let {
            validationError.value = it
            return
        }
        callChangePasswordApi(currentPwd, newPwd)
    }
    /*[END] Below method is just for demostration of error messages live data*/

    private fun callChangePasswordApi(currentPwd: String, newPwd: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()

            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            param[ApiParam.CURRENT_PASSWORD] = AppUtils.SHA1(currentPwd)
            param[ApiParam.NEW_PASSWORD] = AppUtils.SHA1(newPwd)
            subscription = apiServiceObj
                .apiProfile(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: BaseResponse) {
        if(response.status) baseResponse.value = response
        else apiErrorMessage.value = response.message
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