package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import com.krystal.goddesslifestyle.base.BaseViewModel
import io.reactivex.disposables.Disposable

/**
 * Created by imobdev on 30/4/20
 */
class SelectCityStateViewModel(application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}