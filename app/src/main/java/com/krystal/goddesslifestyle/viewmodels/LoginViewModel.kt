package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class LoginViewModel(application: Application) : BaseViewModel(application) {

    var userProfileImage = ""

    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val loginResponse: MutableLiveData<LoginResponse> by lazy {
        MutableLiveData<LoginResponse>()
    }

    fun getLoginResponse(): LiveData<LoginResponse> {
        return loginResponse
    }

    fun callLoginApi(params: HashMap<String, String>,loginWithFab:Boolean=false) {
        if (AppUtils.hasInternet(getApplication())) {
            if (!loginWithFab){
                subscription = apiServiceObj
                    .apiLogin(params)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
            }else{
                /*subscription = apiServiceObj
                    .apiRegistrationSocila(params)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)*/
                val parameters = getParamsRequestBody(params)
                subscription = apiServiceObj
                    .apiRegistration(parameters)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart()}
                    .doOnTerminate { onApiFinish()}
                    .subscribe(this::handleResponse, this::handleError)
            }

        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: LoginResponse) {
        loginResponse.value = response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out))
        }
        //apiErrorMessage.value = error.localizedMessage
    }

    /*[START] User subscription API*/
    private val userSubscriptionResponse: MutableLiveData<UserSubscriptionResponse> by lazy {
        MutableLiveData<UserSubscriptionResponse>()
    }

    fun getUserSubscriptionResponse(): LiveData<UserSubscriptionResponse> {
        return userSubscriptionResponse
    }

    fun callGetUserSubscriptionApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetUserSubscriptions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: UserSubscriptionResponse) {
        userSubscriptionResponse.value = response
    }
    /*[END] User subscription API*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    private fun getParamsRequestBody(params: HashMap<String, String>): HashMap<String, RequestBody> {
        val resultParams = HashMap<String, RequestBody>()

        for ((key, value) in params) {
            val body = value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
            body?.let { resultParams.put(key, it) }
        }

        if (!TextUtils.isEmpty(userProfileImage)) {
            val file = File(userProfileImage)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val imageParams = ApiParam.IMAGES + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }
        return resultParams
    }
}