package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.LikeUnlikeResponse
import com.krystal.goddesslifestyle.data.response.RecipeListReponse
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 23/3/20
 */
class SnacksViewModel (application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    private val dinnerListResponse: MutableLiveData<RecipeListReponse> by lazy {
        MutableLiveData<RecipeListReponse>()
    }

    fun getDinnerListResponseResponse(): LiveData<RecipeListReponse> {
        return dinnerListResponse
    }

    fun callGetRecipeListAPI(pageNo: Int = 1,searchValue:String="",showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, String?> = HashMap()
            params[ApiParam.KEY_PAGE] = pageNo.toString()
            params[ApiParam.KEY_LIMIT] = ApiContants.LIMIT
            params[ApiParam.KEY_RECIPE_TYPE] = ApiContants.RECIPE_TYPE_SNACK
            if (searchValue.isNotEmpty()){
                params[ApiParam.KEY_SEARCH_TEXT] = searchValue
            }
            subscription = apiServiceObj
                .apiGetRecipeList(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: RecipeListReponse) {
        dinnerListResponse.value = response
    }

    private val favouriteResponse: MutableLiveData<LikeUnlikeResponse> by lazy {
        MutableLiveData<LikeUnlikeResponse>()
    }

    fun getFavouriteResponse(): LiveData<LikeUnlikeResponse> {
        return favouriteResponse
    }

    fun callLikeUnlikeRecipeAPI(recipeId: Int, favourite: Int, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.KEY_FAVOURITE] = favourite
            params[ApiParam.ID] = recipeId
            subscription = apiServiceObj
                .apiLikeUnlike(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleFavouriteResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleFavouriteResponse(response: LikeUnlikeResponse) {
        favouriteResponse.value = response
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



    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}