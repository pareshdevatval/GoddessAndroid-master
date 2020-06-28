package com.krystal.goddesslifestyle.di.component

import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.di.module.LocalDataModule
import dagger.Component

//Created by imobdev-rujul on 8/1/19
@Component(modules = [LocalDataModule::class], dependencies = [AppComponent::class])
interface LocalDataComponent {

    fun getPref(): Prefs

    fun getDatabase(): AppDatabase
/*
    fun getApplication(): Application

    fun getContext(): Context*/
}