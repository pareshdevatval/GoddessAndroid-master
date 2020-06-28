package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import com.krystal.goddesslifestyle.base.BaseViewModel
import io.reactivex.disposables.Disposable

/**
 * Created by imobdev on 31/3/20
 */
class HowToAddRecipeViewModel (application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}