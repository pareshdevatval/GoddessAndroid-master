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

class AddAddressModel(application: Application) : BaseViewModel(application) {

    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val addAddressResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getAddAddressResponse(): LiveData<BaseResponse> {
        return addAddressResponse
    }
    private val validationResponse: MutableLiveData<ValidationErrorModel> by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun validationResponse(): LiveData<ValidationErrorModel> {
        return validationResponse
    }
    fun validation(mTitle:String,pincode:String,HouseNo :String,RoadName:String,City:String,State:String,LandMark:String,
                   country: String){
        Validator.validateAaddressTitle(mTitle)?.let {
            validationResponse.value=it
            return
        }
        Validator.validatePinCode(pincode)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateHouseNo(HouseNo)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateRoadName(RoadName)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateCity(City)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateState(State)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateCountry(State)?.let {
            validationResponse.value=it
            return
        }
        Validator.validateCountry(country)?.let {
            validationResponse.value=it
            return
        }

        val params:HashMap<String,Any> = HashMap()
        params[ApiParam.U_ADDRESS_TITLE]=mTitle
        params[ApiParam.U_ADDRESS_PIN_CODE]=pincode
        params[ApiParam.U_ADDRESS_HOUSE_NO]=HouseNo
        params[ApiParam.U_ADDRESS_ROAD_AREA]=RoadName
        params[ApiParam.U_ADDRESS_CITY]=City
        params[ApiParam.U_ADDRESS_STATE]=State
        params[ApiParam.U_ADDRESS_LANDMARK]=LandMark
        params[ApiParam.U_COUNTRY]=country

        callAddAddressApi(params)
    }

    fun callAddAddressApi(params: HashMap<String, Any>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiAddAddress(params)
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
        addAddressResponse.value = response
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