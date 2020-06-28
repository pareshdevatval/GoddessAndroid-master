package com.krystal.goddesslifestyle

import android.app.Application
import com.krystal.goddesslifestyle.di.component.*
import com.krystal.goddesslifestyle.di.module.AppModule
import com.krystal.goddesslifestyle.di.module.LocalDataModule
import com.krystal.goddesslifestyle.di.module.NetworkModule
import com.krystal.goddesslifestyle.utils.BillingManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.StrictMode
//import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
//import io.fabric.sdk.android.Fabric


class GoddessLifeStyleApp : Application() {


    override fun onCreate() {
        super.onCreate()
        //Fabric.with(this, Crashlytics())
        FirebaseApp.initializeApp(this)

        /*A workaround to resolve the crash occuured while playing a taken video in Goddess community opinion
        * video taken screen
        *
        * By below code, VM will ignore the file URI exposure.
        * */
        /*val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())*/

    }



    fun getAppComponent(): AppComponent {
        return DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    fun getLocalDataComponent(): LocalDataComponent {
        return DaggerLocalDataComponent.builder().appComponent(getAppComponent()).localDataModule(LocalDataModule()).build()
    }

    fun getNetworkComponent(): NetworkComponent {
        return DaggerNetworkComponent.builder().appComponent(getAppComponent())
            .networkModule(NetworkModule()).build()
    }

    fun getBillingManager() : BillingManager {
        return BillingManager.getInstance(this)
    }
}