package com.krystal.goddesslifestyle.di.module

import android.app.Application
import android.content.Context
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import dagger.Module
import dagger.Provides

@Module
class AppModule constructor(val application: GoddessLifeStyleApp) {


    @Provides
    fun provideApplication(): Application = application

    /**
     * Provides the Application context.
     * @return the Application context
     */
    @Provides
    fun provideContext(): Context = application.applicationContext

}