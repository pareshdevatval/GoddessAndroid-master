package com.krystal.goddesslifestyle.ui.main_activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.Journal
import com.krystal.goddesslifestyle.databinding.PagerFragmentTodaysJournalBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.TodaysJournalViewModel
import javax.inject.Inject

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class TodaysJournalPagerFagment: BaseFragment<TodaysJournalViewModel>(), View.OnClickListener {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment
        */
        fun newInstance(): TodaysJournalPagerFagment {
            val bundle = Bundle()
            val fragment = TodaysJournalPagerFagment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: TodaysJournalViewModel
    private lateinit var binding: PagerFragmentTodaysJournalBinding

    private var todayJournalId = -1

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    var journal: Journal? = null

    override fun getViewModel(): TodaysJournalViewModel {
        viewModel = ViewModelProvider(this).get(TodaysJournalViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectTodaysJournalPagerFragment(this)

        binding = PagerFragmentTodaysJournalBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.mainLayout.clipToOutline = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInjectable(apiService, prefs)

        viewModel.getShareApiResponse().observe(viewLifecycleOwner, shareApiResponseObserver)
    }

    /*Accepts
    @calenderId : day id for which we have to show Data
    If user selected 9th of the month then in "calenderDayId", the id of 9th date will be passed
    This would be needed to retrieve the data of this date from db*/
    fun setCalenderDay(calenderDayId: Int) {
        // we can get today's journal id from currently selected calenderDay
        todayJournalId = appDatabase.todaysJournalDao().getTodayJournalId(calenderDayId)
        /*And from today's journal id, We can get the object of Journal*/
        journal = appDatabase.journalDao().getJournal(todayJournalId)



        journal?.let { jrnl ->
            binding.imageView.post {
                context?.let {
                    Log.e("JOURNAL_IMAGE", AppUtils.generateImageUrl(jrnl.jpImage, binding.imageView.width,
                        binding.imageView.height))
                    AppUtils.loadImageThroughGlide(it, binding.imageView,
                        AppUtils.generateImageUrl(jrnl.jpImage, binding.imageView.width, binding.imageView.height),
                        R.drawable.ic_placeholder_square)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.shareLayout -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(context, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            if(todayJournalId == -1) {
                                return
                            }

                            if(journal != null && journal!!.jpImage.isNotBlank()) {
                                AppUtils.shareContent(it, AppUtils.generateImageUrl(journal!!.jpImage))
                            } else {
                                AppUtils.shareContent(it, "Share from Goddess LifeStyle Android App")
                            }
                            callShareApi()
                        }
                    }
                }
            }
        }
    }

    private fun callShareApi() {
        val params = HashMap<String, String>()
        params[ApiContants.ACTIVITY_ID] = todayJournalId.toString()
        params[ApiContants.EARNED_POINTS] = ApiContants.SHARE_CALENDER_POINTS.toString()
        params[ApiContants.TYPE] = ApiContants.SHARE_TYPE_CALENDER
        viewModel.callShareApi(params)
    }

    private val shareApiResponseObserver = Observer<BaseResponse> {
        if(it.status) {
            //AppUtils.showToast(context!!, it.message)
        }
    }
}