package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import com.krystal.goddesslifestyle.base.BaseViewModel
import io.reactivex.disposables.Disposable

/**
 * Created by imobdev on 24/3/20
 */
class DetailsViewModel(application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}