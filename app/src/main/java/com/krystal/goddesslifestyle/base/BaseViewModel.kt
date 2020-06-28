package com.krystal.goddesslifestyle.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs


/* A Base class for all ViewModels classes */
/**
 * Created by Bhargav Thanki on 19/12/18.
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val apiErrorMessage: MutableLiveData<String> = MutableLiveData()
    val loadingVisibility: MutableLiveData<Boolean> = MutableLiveData()
    val horizontalPb: MutableLiveData<Boolean> = MutableLiveData()

    protected lateinit var apiServiceObj: ApiService
    protected lateinit var prefsObj: Prefs

    fun onApiStart() {
        loadingVisibility.value = true
        errorMessage.value = null
    }

    fun onInternetError() {
        errorMessage.value = R.string.msg_no_internet
    }

    fun onApiFinish() {
        loadingVisibility.value = false
    }

    fun onApiFinishPost() {
        loadingVisibility.postValue(false)
    }

    fun setInjectable(apiService: ApiService, prefs: Prefs) {
        this.apiServiceObj = apiService
        this.prefsObj = prefs
    }

    fun injectApiService(apiService: ApiService) {
        this.apiServiceObj = apiService
    }

    fun injectPrefs(prefs: Prefs) {
        this.prefsObj = prefs
    }
}