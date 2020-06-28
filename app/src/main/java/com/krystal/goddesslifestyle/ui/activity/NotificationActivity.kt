package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.NotificationAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.GetNotificationListResponse
import com.krystal.goddesslifestyle.databinding.ActivityNotificationBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.NotificationsModel
import javax.inject.Inject

class NotificationActivity : BaseActivity<NotificationsModel>(),
    BaseBindingAdapter.ItemClickListener<GetNotificationListResponse.Result?> {

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var mViewModel: NotificationsModel
    var positions=0

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, NotificationActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): NotificationsModel {
        mViewModel = ViewModelProvider(this).get(NotificationsModel::class.java)
        return mViewModel

    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectNotificationActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        init()
    }

    fun init() {
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.getGetNotificationListResponse().observe(this, getNotificationListObsrerve)
        mViewModel.getRemoveNotificationResponse().observe(this, removeNotificationListObsrerve)
        mViewModel.getClearNotificationResponse().observe(this,clearNotificationListObsrerve)

        binding.rvNotification.layoutManager = LinearLayoutManager(this)
        val notificationAdapter = NotificationAdapter()
        binding.rvNotification.adapter = notificationAdapter
        notificationAdapter.itemClickListener = this
        setToolBar(getString(R.string.lbl_notification), R.color.pink)


        mViewModel.callNotificationListApi()
        loadMore()
    }

    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color

        setToolbarColor(bgColor)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        //setToolbarRightText(R.string.lbl_clear_all)
        binding.toolbarLayout.tvToolbarRight.setOnClickListener {
            /*api call all notification delete*/
            mViewModel.callClearAllNotificationApi()

        }

    }


    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvNotification.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvNotification.layoutManager as LinearLayoutManager,
                true
            ) {
            override fun loadMoreItems() {

                /*If we have load all total pages then no need to go for next*/
                if (TOTAL_PAGE != CURRENT_PAGE) {
                    /*checking for loading flag to prevent too many calls at same time*/
                    if (isLoading) {
                        /*switching the value of loading flag*/
                        isLoading = false
                        /*increment the current page count*/
                        CURRENT_PAGE += 1

                        /*Adding a dummy(null) item to the list to show a footer progress bar*/
                        (binding.rvNotification.adapter as NotificationAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvNotification.adapter as NotificationAdapter).itemCount
                        (binding.rvNotification.adapter as NotificationAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvNotification.adapter as NotificationAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callNotificationListApi(CURRENT_PAGE, false)
                    }
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGE
            }

            override fun isLastPage(): Boolean {
                return false
            }

            override fun isLoading(): Boolean {
                return false
            }

        })

    }

    /*observer notification list data*/
    private val getNotificationListObsrerve = Observer<GetNotificationListResponse> {
            if (it.status) {
                /*set ounread count*/
                if (it.unreadCount == 0) {
                    binding.tvUnReadCount.visibility = View.GONE
                } else {
                    binding.tvUnReadCount.visibility = View.VISIBLE
                    binding.tvUnReadCount.text =
                        String.format(getString(R.string.lbl_unread_count), it.unreadCount)
                }
                if (CURRENT_PAGE == 1) {
                    TOTAL_PAGE = it.pagination.lastPage

                    if (it.result.isNotEmpty()) {
                        binding.rvNotification.visibility = View.VISIBLE
                        binding.llNoData.visibility = View.GONE
                        (binding.rvNotification.adapter as NotificationAdapter).setItem(it.result)
                        setToolbarRightText(R.string.lbl_clear_all)
                    } else {
                        binding.clNotificationData.visibility = View.GONE
                        binding.llNoData.visibility = View.VISIBLE
                        AppUtils.startNotificationWaveAnimation(
                            this,
                            binding.viewNoNotification.wave,
                            R.color.pink
                        )
                        hideToolbarRightText()
                    }
                } else {
                    (binding.rvNotification.adapter as NotificationAdapter).removeLoadingFooter(true)
                    val adapterCount =
                        (binding.rvNotification.adapter as NotificationAdapter).itemCount
                    (binding.rvNotification.adapter as NotificationAdapter).removeItem(
                        adapterCount - 1
                    )


                    val sizeList =
                        (binding.rvNotification.adapter as NotificationAdapter).itemCount
                    (binding.rvNotification.adapter as NotificationAdapter).addItems(it.result)
                    (binding.rvNotification.adapter as NotificationAdapter).notifyItemRangeInserted(
                        sizeList,
                        it.result.size
                    )
                    isLoading=true
                }

            } else {
                AppUtils.showSnackBar(binding.rvNotification, it.message)
            }
        }

    /*observer single notification delete data*/
    private val removeNotificationListObsrerve = Observer<BaseResponse> {
        if(it.status){
            (binding.rvNotification.adapter as NotificationAdapter).removeItem(positions)

            if((binding.rvNotification.adapter as NotificationAdapter).items.size==0){
                hideToolbarRightText()
                binding.clNotificationData.visibility = View.GONE
                binding.llNoData.visibility = View.VISIBLE
                AppUtils.startNotificationWaveAnimation(
                    this,
                    binding.viewNoNotification.wave,
                    R.color.pink
                )
            }
        }
    }

    /*observer all notification delete data*/
    private val clearNotificationListObsrerve = Observer<BaseResponse> {
        if(it.status){
            (binding.rvNotification.adapter as NotificationAdapter).clear()
            (binding.rvNotification.adapter as NotificationAdapter).notifyDataSetChanged()

            binding.clNotificationData.visibility = View.GONE
            binding.llNoData.visibility = View.VISIBLE

            hideToolbarRightText()

            AppUtils.startNotificationWaveAnimation(
                this,
                binding.viewNoNotification.wave,
                R.color.pink
            )
        }
    }

    override fun onItemClick(view: View, data: GetNotificationListResponse.Result?, position: Int) {
        when (view.id) {
            R.id.ivDelete -> {
                positions=position
                data?.let {
                    /*api call single notification delete*/
                    mViewModel.callRemoveNotificationApi(data.nId)
                }

            }
        }
    }

}
