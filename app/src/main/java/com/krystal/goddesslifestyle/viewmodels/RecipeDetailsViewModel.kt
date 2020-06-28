package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.RecipeDetailsResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 24/3/20
 */
class RecipeDetailsViewModel (application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val recipeDetailsResponse: MutableLiveData<RecipeDetailsResponse> by lazy {
        MutableLiveData<RecipeDetailsResponse>()
    }

    fun getRecipeDetailsResponse(): LiveData<RecipeDetailsResponse> {
        return recipeDetailsResponse
    }

    fun callGetRecipeDetailsAPI(recipeId:String) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, String?> = HashMap()
            params[ApiParam.ID]=recipeId

            subscription = apiServiceObj
                .apiGetRecipeDetails(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onApiStart() }
                .doOnTerminate { onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: RecipeDetailsResponse) {
        recipeDetailsResponse.value=response
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if(error is SocketTimeoutException) {
            AppUtils.showToast(getApplication(), getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out))
        }
    }


    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}