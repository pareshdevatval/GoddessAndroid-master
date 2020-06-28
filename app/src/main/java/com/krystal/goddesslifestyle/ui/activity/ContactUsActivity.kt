package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.georeminder.utils.validator.ValidationErrorModel
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityContactUsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.ContactUsModel
import javax.inject.Inject

class ContactUsActivity : BaseActivity<ContactUsModel>(), View.OnClickListener {

    private lateinit var binding: ActivityContactUsBinding
    private lateinit var mViewModel: ContactUsModel


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
            val intent = Intent(context, ContactUsActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): ContactUsModel {
        mViewModel = ViewModelProvider(this).get(ContactUsModel::class.java)
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
        requestsComponent.injectContactUsActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us)
        init()
    }

    fun init() {
        mViewModel.setInjectable(apiService, prefs)
        binding.btnSend.setOnClickListener(this)
        mViewModel.getBaseResponse().observe(this, contactUsObserve)
        mViewModel.getValidationError().observe(this, valiadtionObserve)
        setToolBar(R.color.white)

    }
    /*observer contact us data*/
    private val contactUsObserve = Observer<BaseResponse> {
        AppUtils.showSnackBar(binding.btnSend,it.message)
        binding.edtEmail.setText("")
        binding.edtTitle.setText("")
        binding.edtMessage.setText("")
    }
    /*observer validation*/
    private val valiadtionObserve = Observer<ValidationErrorModel> {
        AppUtils.showSnackBar(binding.btnSend,getString(it.msg))
    }

    private fun setToolBar(bgColor: Int) {
        // setting toolbar title
        setToolbarTitle("")
        // toolbar color

        setToolbarColor(bgColor)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back_pink, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (it.id) {
                R.id.btnSend -> {
                    //api call
                    mViewModel.checkValidation(AppUtils.getText(binding.edtTitle),
                        AppUtils.getText(binding.edtEmail),AppUtils.getText(binding.edtMessage))
                }
            }
        }
    }


}
