package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

/**
 * Created by imobdev on 21/2/20
 */
class MyPlansViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var appDatabase: AppDatabase

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    fun getUserSubscription() : UserSubscription? {
        val user = prefsObj.userDataModel
        user?.let {
            it.result?.let {userData ->
                val userSubscriptions = appDatabase.userSubscriptionDao().getUserSubscriptionData(userData.uId!!)
                if(userSubscriptions.isEmpty()) {
                    return null
                } else {
                    return userSubscriptions[0]
                }
            }
        }
        return null
    }

}