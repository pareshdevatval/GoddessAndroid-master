package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.EditProfileResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.net.SocketTimeoutException

class EditProfileModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null
    var userImages: String = ""

    private val editProfileResponse: MutableLiveData<EditProfileResponse> by lazy {
        MutableLiveData<EditProfileResponse>()
    }

    fun getEditProfileResponse(): LiveData<EditProfileResponse> {
        return editProfileResponse
    }


    fun callEditProfileApi(params: HashMap<String, String?>) {
        if (AppUtils.hasInternet(getApplication())) {
            val parameters = getParamsRequestBody(params)
            subscription = apiServiceObj
                .apiEditProfile(parameters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart()}
                .doOnTerminate { onApiFinish()}
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: EditProfileResponse) {
        editProfileResponse.value = response
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

    private fun getParamsRequestBody(params: HashMap<String, String?>): HashMap<String, RequestBody> {
        val resultParams = HashMap<String, RequestBody>()

        for ((key, value) in params) {
            val body = value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
            body?.let { resultParams.put(key, it) }
        }

        if (!TextUtils.isEmpty(userImages)) {
            val file = File(userImages)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val imageParams = ApiParam.IMAGES + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }
        return resultParams
    }
}