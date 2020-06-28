package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.ForgetPasswordResponse
import com.krystal.goddesslifestyle.databinding.ActivityForgetPasswordBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.ForgetPasswordModel
import javax.inject.Inject

class ForgetPasswordActivity : BaseActivity<ForgetPasswordModel>() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var mViewModel: ForgetPasswordModel

    lateinit var editTextForgetEmail : EditText

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
            val intent = Intent(context, ForgetPasswordActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): ForgetPasswordModel {
        mViewModel = ViewModelProvider(this).get(ForgetPasswordModel::class.java)
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
        requestsComponent.injectForgetPasswordActivity(this)
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_forget_password)
        editTextForgetEmail= findViewById(R.id.edtEmail)
        mViewModel.setInjectable(apiService,prefs)
        mViewModel.getForgetPasswordResponse().observe(this, forgetPasswordResponseObserver)

        /*check validation email address*/
        binding.btnSend.setOnClickListener {

            if(editTextForgetEmail.text.toString() == ""){
                AppUtils.showSnackBar(it,"Please enter email")
            }
            else if(!isEmailValid(editTextForgetEmail.text.toString())){
                AppUtils.showSnackBar(it,"Please enter valid email")
            }
            else{
                val requestParams = HashMap<String, String>()
                requestParams[ApiContants.EMAIL] = editTextForgetEmail.text.toString()
                mViewModel.callForgetPasswordApi(requestParams)
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    /*observer forgot password data*/
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private val forgetPasswordResponseObserver = Observer<ForgetPasswordResponse> { resoponse ->
        if(resoponse.status) {
            AppUtils.showSnackBar(binding.btnSend, resoponse.message)
        } else {
            AppUtils.showSnackBar(binding.btnSend, resoponse.message)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }
}
