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
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class ContactUsModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val contactusResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getBaseResponse(): LiveData<BaseResponse> {
        return contactusResponse
    }

    private val validationError: MutableLiveData<ValidationErrorModel> by lazy {
        MutableLiveData<ValidationErrorModel>()
    }


    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun checkValidation(mTitle :String,mEmail:String,mMessage:String){
        Validator.validateTitle(mTitle)?.let {
            validationError.value=it
            return
        }
        Validator.validateEmail(mEmail)?.let {
            validationError.value=it
            return
        }
        Validator.validateDescription(mMessage)?.let {
            validationError.value=it
            return
        }
        val params :HashMap<String,Any> = HashMap()
        params[ApiParam.CON_TITLE]=mTitle
        params[ApiParam.CON_EMAIL]=mEmail
        params[ApiParam.CON_MESSAGE]=mMessage
        callContactUsApi(params)
    }


    fun callContactUsApi(params: HashMap<String, Any>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiContactUs(params)
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
        contactusResponse.value = response
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