package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.CommunityOpinionCommentListResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 31/3/20
 */
class CommentsViewModel (application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null
    private var opinionId:Int=0
    private var currentPage:Int=1
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    private val opinionsCommentResponse: MutableLiveData<CommunityOpinionCommentListResponse> by lazy {
        MutableLiveData<CommunityOpinionCommentListResponse>()
    }

    fun getCommunityOpinionCommentResponse(): LiveData<CommunityOpinionCommentListResponse> {
        return opinionsCommentResponse
    }


    private val addCommentResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getAddCommentResponse(): LiveData<BaseResponse> {
        return addCommentResponse
    }

    fun callGetCommunityCommentListAPI(opinionId: Int,pageNo: Int = 1, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {

            val params: HashMap<String, Any?> = HashMap()
            params[ApiParam.ID]=opinionId
            params[ApiParam.KEY_PAGE] = pageNo.toString()


            subscription = apiServiceObj
                .apiGetCommunityOpinionComment(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: CommunityOpinionCommentListResponse) {
        opinionsCommentResponse.value = response
    }


    private fun callSendCommentAPI(opinionId: Int, comment: String, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.ID] = opinionId
            params[ApiParam.KEY_COMMENT] = comment

            subscription = apiServiceObj
                .apiAddCommunityOpinionComment(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleSendCommentResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleSendCommentResponse(response: BaseResponse) {
        addCommentResponse.value = response
        /*if (response.status) {
            callGetCommunityCommentListAPI(this.opinionId, this.currentPage)
        } else {
            apiErrorMessage.value = response.message
        }*/
    }

    fun checkCommentValidation(context:Context,opinionId: Int, comment: String,
                               currentPage: Int) = if (comment.isNotEmpty()){
        callSendCommentAPI(opinionId,comment)
        this.opinionId=opinionId
        this.currentPage = currentPage
    }else{
        apiErrorMessage.value=context.getString(R.string.msg_enter_comment)
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
}