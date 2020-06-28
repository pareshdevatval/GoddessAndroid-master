package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import android.os.AsyncTask
import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.CalenderDay
import com.krystal.goddesslifestyle.data.response.*
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by Bhargav Thanki on 20 February,2020.
 */
class HomeViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null
    private lateinit var appDatabase: AppDatabase

    val configLoadingVisibility: MutableLiveData<Boolean> = MutableLiveData()

    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val updateTokenResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun geTokenResponse(): LiveData<BaseResponse> {
        return updateTokenResponse
    }

    /*Getting current month name in MMM format, to display in header*/
    fun getCurrentMonthName(): String {
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMM", Locale.US)
        return sdf.format(calender.time)
    }

    /*getting current month days*/
    fun getCurrentMonthCalender(theme: Theme): ArrayList<CalenderDay> {
        val calender = Calendar.getInstance()
        /*This will return the number of days in the month*/
        val daysInMonth = calender.getActualMaximum(Calendar.DAY_OF_MONTH)
        /*Adding all the dates in an arraylist*/
        val monthDates: ArrayList<CalenderDay> = ArrayList()
        val monthDateLabels: ArrayList<String> = getCalenderDayNames(theme.themeId)
        try {
            for (i in 1..daysInMonth) {
                monthDates.add(CalenderDay(i, monthDateLabels[i - 1]))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /*[START] Logic to get the empty cells before the 1st date of the month*/

        // getting timestamp of the Date 1 of current month
        calender.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = calender.time
        // Date formatter to get the day name on date 1st
        val sdf = SimpleDateFormat("EEE", Locale.US)
        val day = sdf.format(firstDay)

        // Now, we can count the previous days from curret day
        var emptyDayCount = 0

        when (day) {
            "Sun" -> {
                emptyDayCount = 0
            }
            "Mon" -> {
                emptyDayCount = 1
            }
            "Tue" -> {
                emptyDayCount = 2
            }
            "Wed" -> {
                emptyDayCount = 3
            }
            "Thu" -> {
                emptyDayCount = 4
            }
            "Fri" -> {
                emptyDayCount = 5
            }
            "Sat" -> {
                emptyDayCount = 6
            }
        }

        /*Adding all previous days in arraylist with date 0*/
        if (emptyDayCount > 0) {
            for (j in 1..emptyDayCount) {
                monthDates.add(0, CalenderDay(0, ""))
            }
        }
        /*[END] Logic to get the empty cells before the 1st date of the month*/


        val calender2 = Calendar.getInstance()
        // getting timestamp of the last Date of current month
        calender2.set(Calendar.DAY_OF_MONTH, calender2.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDay = calender2.time
        // Date formatter to get the day name on date 1st
        val sdfLast = SimpleDateFormat("EEE", Locale.US)
        val dayLast = sdfLast.format(lastDay)

        // Now, we can count the previous days from curret day
        var lastEmptyDayCount = 0

        when (dayLast) {
            "Sun" -> {
                lastEmptyDayCount = 6
            }
            "Mon" -> {
                lastEmptyDayCount = 5
            }
            "Tue" -> {
                lastEmptyDayCount = 4
            }
            "Wed" -> {
                lastEmptyDayCount = 3
            }
            "Thu" -> {
                lastEmptyDayCount = 2
            }
            "Fri" -> {
                lastEmptyDayCount = 1
            }
            "Sat" -> {
                lastEmptyDayCount = 0
            }
        }

        /*Adding all previous days in arraylist with date 0*/
        if (lastEmptyDayCount > 0) {
            for (j in 1..lastEmptyDayCount) {
                monthDates.add(CalenderDay(0, ""))
            }
        }

        /*returnig total month days including the in-dates count*/
        return monthDates
    }

    fun getThisMonthTheme(): Theme? {
        val calender = Calendar.getInstance()
        // We are adding 1 here because Calendar.MONTH values starts from 0 and not from 1
        // and for readable format, we start the from 1 in our app
        val currentMonth = calender.get(Calendar.MONTH) + 1
        val currentYear = calender.get(Calendar.YEAR)

        appDatabase.themeMasterDao().getThemeMaster(currentYear, currentMonth)?.let {
            if (appDatabase.themeDao().getCurrentMonthTheme(it.taThemeId) == null)
                apiErrorMessage.value = "There is no calendar data available"
            else
                return appDatabase.themeDao().getCurrentMonthTheme(it.taThemeId)
        }
        return null
    }

    fun getThemeId(): Int {
        val theme = getThisMonthTheme()
        theme?.let {
            return theme.themeId
        }
        return -1
    }

    private fun getCalenderDayNames(themeId: Int): ArrayList<String> {
        return appDatabase.calenderDataDao().getDayLabels(themeId) as ArrayList<String>
    }

    /*[START] Code from Splash Activity to get the calender Data*/
    private val isCalenderDataStored: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun observeIfCalenderDataStored(): LiveData<Boolean> {
        return isCalenderDataStored
    }

    /*Method to check if data is inserted in DB for this month or not*/
    fun isDataLocallyAvailable(): Boolean {
        val calender = Calendar.getInstance()
        val currentMonth = calender.get(Calendar.MONTH) + 1
        val currentYear = calender.get(Calendar.YEAR)

        appDatabase.themeMasterDao().getThemeMaster(currentYear, currentMonth)?.let {
            return true
        }
        return false
    }

    fun getCalenderDataFromApi() {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiGetCalenderData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onConfigStart() }
                .doOnTerminate { }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun callSyncCalenderApi() {
        if (AppUtils.hasInternet(getApplication())) {
            val params = HashMap<String, String>()
            params[ApiParam.KEY_SYNC_DATE] = prefsObj.syncDate!!
            // params[ApiParam.KEY_SYNC_DATE] = "2020-06-09 16:01:29"
            subscription = apiServiceObj
                .apiSyncCalenderData(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { }
                .doOnTerminate { }
                .subscribe(this::handleSyncResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleSyncResponse(response: SyncCalenderResponse) {
        AsyncTask.execute(Runnable {
            updateDataInBackgroundThread(response)
            prefsObj.syncDate = response.server_date
            callSyncCalenderEventApi()
        })
    }

    fun callSyncCalenderEventApi() {
        if (AppUtils.hasInternet(getApplication())) {
            val params = HashMap<String, String>()
            params[ApiParam.KEY_SYNC_DATE] = prefsObj.syncCalenderEventDate!!
            // params[ApiParam.KEY_SYNC_DATE] = "2020-06-09 16:01:29"
            subscription = apiServiceObj
                .apiSyncCalenderEventData(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { }
                .doOnTerminate { }
                .subscribe(this::handleSyncCalenderEventResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleSyncCalenderEventResponse(response: SyncCalenderEventResponse) {
        AsyncTask.execute(Runnable {
            prefsObj.syncCalenderEventDate = response.server_date
            Observable.just(response)
                .map {
                    for (calenderData in it.result!!) {
                        if (calenderData != null) {
                            insertCalenderDataToDatabase(calenderData)
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        })
    }


    private fun updateDataInBackgroundThread(response: SyncCalenderResponse) {
        Observable.just(response)
            .map {
                val calenderData = response.result!!.themes_data!!.calendars_data
                if (calenderData != null) {
                    if (calenderData.calendar_recipes != null) {
                        insertOrUpdateRecipeData(calenderData.calendar_recipes!!)
                    }
                    if (calenderData.calendar_practics != null) {
                        insertOrUpdatePracticeData(calenderData.calendar_practics!!)
                    }
                    if (calenderData.calendar_journal_prompts != null) {
                        insertOrUpdateJournalData(calenderData.calendar_journal_prompts!!)
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun getUpdateTokeApi(userId: Int, token: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params["u_id"] = userId
            params[ApiParam.DEVICE_TOKEN] = token
            params[ApiParam.DEVICE_TYPE] = "android"
            subscription = apiServiceObj
                .apiUpDateToken(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { onConfigStart() }
                .doOnTerminate { }
                .subscribe(this::handleUdateResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun onConfigStart() {
        configLoadingVisibility.value = true
    }

    fun onConfigEnd() {
        configLoadingVisibility.value = false
    }

    private fun handleResponse(response: CalenderResponse) {
        AsyncTask.execute(Runnable {
            insertDataInBackgroundThread(response)
            prefsObj.syncDate = response.serverDate
        })
    }

    private fun handleUdateResponse(response: BaseResponse) {
        updateTokenResponse.value = response
    }


    private fun insertDataInBackgroundThread(response: CalenderResponse) {
        /*if(response.status) calenderResponse.value = response
        else apiErrorMessage.value = response.message*/
        /*First thing first
        * Check for status returned in response*/
        if (response.status) {
            /*Checking for theme master data first
            * This will contain the theme id for current month and yer*/
            response.themeMaster?.let { themeMaster ->
                /*inserting the theme Master data to Room Database*/
                appDatabase.themeMasterDao().insert(themeMaster)
                /*Getting theme Data from Theme master object*/
                themeMaster.themesData?.let { theme ->
                    /*Insert theme to db*/
                    appDatabase.themeDao().insert(theme)

                    /*Theme will contain the calender Data*/
                    theme.calendarsData?.let { calenderData ->
                        /*Calender data would be an individual object per data.
                        * So this loop will iterate for the DAY_OF_MONTH times*/
                        for (calender in calenderData) {
                            calender?.let { cal ->
                                // insert calender Data
                                insertCalenderDataToDatabase(cal)
                            }

                        }


                    }
                }
            }
            isCalenderDataStored.postValue(true)
        } else {
            apiErrorMessage.postValue(response.message)
        }
        //onApiFinishPost()
    }

    private fun insertCalenderDataToDatabase(cal: CalendarsData) {
        appDatabase.calenderDataDao().insert(cal)

        /*Getting Today's Practice*/
        cal.calendarPractics?.let { todayPractices ->
            /*Iterate over all practices of today
                                                            * Currently it is a single practice currently, but in future requirment gets changes,
                                                            * Then we cal tackle with that*/
            for (todayPractice in todayPractices) {
                todayPractice?.let { todaysPractice ->
                    /*insert today practice*/
                    appDatabase.todaysPracticeDao().insert(todaysPractice)

                    /*Getting practices from today practice*/
                    todaysPractice.practics?.let { practices ->
                        insertOrUpdatePracticeData(practices)
                    }
                }
            }
        }

        /*Getting today's RecipeOfTheMonth*/
        cal.calendarRecipes?.let { todayRecipes ->
            for (todayRecipe in todayRecipes) {
                todayRecipe?.let { todayRec ->
                    /*insert today RecipeOfTheMonth*/
                    appDatabase.todaysRecipeDao().insert(todayRec)

                    /*Getting recipes*/
                    todayRec.recipes?.let { recipes ->
                        insertOrUpdateRecipeData(recipes)
                    }
                }
            }

            /*Same way, Getting today's journals*/
            cal.calendarJournalPrompts?.let { todayJournals ->
                for (todayJournal in todayJournals) {
                    todayJournal?.let { todJournal ->
                        /*insert today Journal's data*/
                        appDatabase.todaysJournalDao().insert(todJournal)

                        /*Iterating over journals*/
                        todJournal.journalPrompts?.let { journals ->
                            insertOrUpdateJournalData(journals)
                        }
                    }
                }
            }
        }
    }

    private fun insertOrUpdateJournalData(journals: List<Journal?>) {
        for (journal in journals) {
            journal?.let { jrnl ->
                /*inseting journals*/
                appDatabase.journalDao().insert(jrnl)
            }
        }
    }

    private fun insertOrUpdatePracticeData(practices: List<Practice?>) {
        for (practice in practices) {
            practice?.let { prac ->
                // insert practice to db
                appDatabase.practiceDao().insert(prac)

                /*Iterating over practice equipments*/
                prac.practiceEquipments?.let { practiceEquipments ->
                    for (practiceEquipment in practiceEquipments) {
                        practiceEquipment?.let { pracEquip ->
                            /*Insert practice equipments*/
                            appDatabase.practiceEquipmentDao()
                                .insert(pracEquip)

                            /*Iterating over practices*/
                            pracEquip.equipments?.let { equipments ->
                                for (equipement in equipments) {
                                    /*Insert practices to db*/
                                    equipement?.let { equip ->
                                        appDatabase.equipmentDao()
                                            .insert(equip)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun insertOrUpdateRecipeData(recipes: List<Recipe?>) {
        for (recipe in recipes) {
            recipe?.let { rec ->
                /*insert RecipeOfTheMonth*/
                appDatabase.recipeDao().insert(rec)

                /*Getting recipe images*/
                rec.recipeImages?.let { recipeImages ->
                    for (recipeImage in recipeImages) {
                        recipeImage?.let { recImg ->
                            /*insert recipe images*/
                            appDatabase.recipeImageDao()
                                .insert(recImg)
                        }
                    }
                }
                /*Getting recipe steps*/
                rec.recipeSteps?.let { recipeSteps ->
                    for (recipeStep in recipeSteps) {
                        recipeStep?.let { recStep ->
                            /*Insert recipe step*/
                            appDatabase.recipeStepDao()
                                .insert(recStep)
                        }
                    }
                }
            }
        }
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
        //apiErrorMessage.value = error.localizedMessage
    }
    /*[END] Code from Splash Activity to get the calender Data*/

    /*[START] code to call share points API*/
    private val shareApiResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getShareApiResponse(): LiveData<BaseResponse> {
        return shareApiResponse
    }

    fun callShareApi(params: HashMap<String, String>) {
        if (AppUtils.hasInternet(getApplication())) {
            subscription = apiServiceObj
                .apiAddUserPoints(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /*onApiStart()*/ }
                .doOnTerminate { /*onApiFinish()*/ }
                .subscribe(this::handleShareResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleShareResponse(response: BaseResponse) {
        this.shareApiResponse.value = response
    }


    fun clearCartData() {
        appDatabase.cartDao().nukeTable()
        appDatabase.cartProductDao().nukeTable()
        appDatabase.cartAmountDao().nukeTable()
    }

    /*[END] code to call share points API*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

}