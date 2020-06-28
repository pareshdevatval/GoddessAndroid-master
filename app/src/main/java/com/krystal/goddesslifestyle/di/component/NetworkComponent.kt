package com.krystal.goddesslifestyle.di.component

import android.app.Application
import android.content.Context
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.di.module.NetworkModule
import dagger.Component

//Created by imobdev-rujul on 7/1/19
@Component(modules = [NetworkModule::class], dependencies = [AppComponent::class])
interface NetworkComponent {

    fun getApiClient(): ApiService

    fun getApplication(): Application

    fun getContext(): Context

}