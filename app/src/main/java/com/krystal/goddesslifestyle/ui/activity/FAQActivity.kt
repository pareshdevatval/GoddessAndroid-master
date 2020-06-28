package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.FAQAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityFaqBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.viewmodels.FAQModel
import javax.inject.Inject

class FAQActivity : BaseActivity<FAQModel>(),
    BaseBindingAdapter.ItemClickListener<Boolean?> {
    override fun onItemClick(view: View, data: Boolean?, position: Int) {

    }

    private lateinit var binding: ActivityFaqBinding
    private lateinit var mViewModel: FAQModel


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
            val intent = Intent(context, FAQActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): FAQModel {
        mViewModel = ViewModelProvider(this).get(FAQModel::class.java)
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
        requestsComponent.injectFAQActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faq)
        init()
    }

    fun init() {
        binding.rvFaq.layoutManager = LinearLayoutManager(this)
        val faqAdapter = FAQAdapter()
        binding.rvFaq.adapter = faqAdapter
        faqAdapter.itemClickListener = this

        setToolBar(getString(R.string.lbl_faq), R.color.pink)

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

    }

}
