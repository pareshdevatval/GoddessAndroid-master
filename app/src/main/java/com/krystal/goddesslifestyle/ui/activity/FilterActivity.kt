package com.krystal.goddesslifestyle.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityFilterBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.viewmodels.FilterModel
import javax.inject.Inject

class FilterActivity : BaseActivity<FilterModel>(), View.OnClickListener {

    private lateinit var binding: ActivityFilterBinding
    private lateinit var mViewModel: FilterModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    private val mTime: Int by lazy {
        intent.getIntExtra(ApiParam.TIME_FILTER, 0)
    }
    private val mOrder: Int by lazy {
        intent.getIntExtra(ApiParam.ORDER_FILTER, 0)
    }

    var time = 0
    var order = 0

    companion object {
        fun newInstance(context: Context, time: Int, order: Int): Intent {
            val intent = Intent(context, FilterActivity::class.java)
            intent.putExtra(ApiParam.TIME_FILTER, time)
            intent.putExtra(ApiParam.ORDER_FILTER, order)
            return intent
        }
    }

    override fun getViewModel(): FilterModel {
        mViewModel = ViewModelProvider(this).get(FilterModel::class.java)
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
        requestsComponent.injectFilterActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter)
        init()
    }

    fun init() {
        /*set selected value*/
        time = mTime  //set selected time
        order = mOrder // set selected order
        setToolBar(getString(R.string.lbl_filter), R.color.yellow)
        binding.btnApply.setOnClickListener(this)
        setSelectedValue()
        radioButtonSelection()
    }

    private fun radioButtonSelection() {
        binding.radioGroupOrder.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbOpenOrder -> {
                    order = 1
                }
                R.id.rbPreparing -> {
                    order = 2
                }
                R.id.rbReady -> {
                    order = 3
                }
                R.id.rbDispatch -> {
                    order = 4
                }
                R.id.rbDeliveryOrder -> {
                    order = 5
                }
                R.id.rbAllOrder -> {
                    order = 0
                }
            }
        }
        binding.radioGroupTime.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbLastOrder -> {
                    time = 1
                }
                R.id.rbLast30Day -> {
                    time = 2
                }
                R.id.rb2019 -> {
                    time = 3
                }
                R.id.rb2018 -> {
                    time = 4
                }
            }
        }
    }
    /*set selected item */
    private fun setSelectedValue() {
        when (order) {
            0 -> binding.rbAllOrder.isChecked = true
            1 -> binding.rbOpenOrder.isChecked = true
            2 -> binding.rbPreparing.isChecked = true
            3 -> binding.rbReady.isChecked = true
            4 -> binding.rbDispatch.isChecked = true
            5 -> binding.rbDeliveryOrder.isChecked = true
        }

        when (time) {
            1 -> binding.rbLastOrder.isChecked = true
            2 -> binding.rbLast30Day.isChecked = true
            3 -> binding.rb2019.isChecked = true
            4 -> binding.rb2018.isChecked = true
        }
    }

    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color

        setToolbarColor(bgColor)

        // toolbar right icon and its click listener
        binding.toolbarLayout.tvToolbarLeft.text="Reset"
        binding.toolbarLayout.tvToolbarLeft.setOnClickListener {
            time = 0
            order = 0
            closeActivity()
        }
        setToolbarRightIcon(
            R.drawable.ic_cross_white,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    onBackPressed()
                }
            })

    }

    fun closeActivity() {
        val returnIntent = Intent()
        returnIntent.putExtra("time", time)
        returnIntent.putExtra("order", order)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when (v?.id) {
            R.id.btnApply -> {
                closeActivity()
            }
        }
    }

}
