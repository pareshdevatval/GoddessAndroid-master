package com.krystal.goddesslifestyle.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityChangePasswordBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.ChangesPasswordModel
import javax.inject.Inject

class ChangesPasswordActivity : BaseActivity<ChangesPasswordModel>(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (it.id) {
                R.id.btnSave -> {
                    if (binding.edtOldPassword.text.toString().isEmpty()) {
                        AppUtils.showSnackBar(it, "Please enter old password")
                        return
                    }
                    if (binding.edtOldPassword.text.toString().trim().length < 8) {
                        AppUtils.showSnackBar(it, "Old password length must be 8 characters")
                        return
                    }
                    if (binding.edtNewPassword.text.toString().isEmpty()) {
                        AppUtils.showSnackBar(it, "Please enter new password")
                        return
                    }
                    if (binding.edtNewPassword.text.toString().trim().length < 8) {
                        AppUtils.showSnackBar(it, "New password length must be 8 characters")
                        return
                    }
                    if (binding.etConfirmPassword.text.toString().isEmpty()) {
                        AppUtils.showSnackBar(it, "Please enter confirm password")
                        return
                    }
                    if (binding.etConfirmPassword.text.toString().trim().length < 8) {
                        AppUtils.showSnackBar(it, "Confirm password length must be 8 characters")
                        return
                    }
                    if (binding.edtNewPassword.text.toString().trim() != binding.etConfirmPassword.text.toString().trim()) {
                        AppUtils.showSnackBar(it, "Password and confirm password must be same")
                        return
                    }
                    val param: HashMap<String, Any> = HashMap()
                    param[ApiParam.CURRENT_PASSWORD] = AppUtils.getText(binding.edtOldPassword)
                    param[ApiParam.NEW_PASSWORD] = AppUtils.getText(binding.edtNewPassword)
                    mViewModel.callForgetPasswordApi(param)

                }
                R.id.ivBack -> {
                    onBackPressed()
                }
            }
        }

    }

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var mViewModel: ChangesPasswordModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    private var isOldShowPassword = false
    private var isNewShowConfirmPassword = false
    private var isShowConfirmPassword = false
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, ChangesPasswordActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): ChangesPasswordModel {
        mViewModel = ViewModelProvider(this).get(ChangesPasswordModel::class.java)
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
        requestsComponent.injectChangesPasswordActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        mViewModel.setInjectable(apiService, prefs)
        init()
    }

    fun init() {
        binding.btnSave.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        mViewModel.getChangePasswordResponse().observe(this, changesPassObserve)
        setHideShowPassword()
    }

    private val changesPassObserve = Observer<BaseResponse> {

        AppUtils.showSnackBar(binding.btnSave, it.message)
        if (it.status) {
            prefs.password = binding.edtNewPassword.text.toString()
            binding.edtNewPassword.setText("")
            binding.edtOldPassword.setText("")
            binding.etConfirmPassword.setText("")
        }

    }



    @SuppressLint("ClickableViewAccessibility")
    private fun setHideShowPassword() {
        //hide and show password
        binding.edtOldPassword.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val DRAWABLE_RIGHT = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.edtOldPassword.right - binding.edtOldPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isOldShowPassword) {
                            binding.edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_show),
                                null
                            )
                            isOldShowPassword = false
                            binding.edtOldPassword.clearFocus()
                            binding.edtOldPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtOldPassword.setTextAppearance(this@ChangesPasswordActivity, R.style.editText_style)
                            binding.edtOldPassword.setSelection(binding.edtOldPassword.text!!.length)
                        } else {
                            binding.edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_hide),
                                null
                            )
                            isOldShowPassword = true
                            binding.edtOldPassword.clearFocus()
                            binding.edtOldPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            binding.edtOldPassword.setTextAppearance(
                                this@ChangesPasswordActivity,
                                R.style.editText_style
                            )
                            binding.edtOldPassword.setSelection(binding.edtOldPassword.text!!.length)
                        }
                        return true
                    }
                }
                return false
            }
        })

        binding.edtNewPassword.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                val DRAWABLE_RIGHT = 2

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.edtNewPassword.right - binding.edtNewPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isNewShowConfirmPassword) {
                            binding.edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_show),
                                null
                            )
                            isNewShowConfirmPassword = false
                            binding.edtNewPassword.clearFocus()
                            binding.edtNewPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtNewPassword.setTextAppearance(this@ChangesPasswordActivity, R.style.editText_style)
                            binding.edtNewPassword.setSelection(binding.edtNewPassword.text!!.length)
                        } else {
                            binding.edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_hide),
                                null
                            )
                            isNewShowConfirmPassword = true
                            binding.edtNewPassword.clearFocus()
                            binding.edtNewPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            binding.edtNewPassword.setTextAppearance(
                                this@ChangesPasswordActivity,
                                R.style.editText_style
                            )
                            binding.edtNewPassword.setSelection(binding.edtNewPassword.text!!.length)
                        }
                        return true
                    }
                }
                return false
            }
        })

        binding.etConfirmPassword.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                val DRAWABLE_RIGHT = 2

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.etConfirmPassword.right - binding.etConfirmPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isShowConfirmPassword) {
                            binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_show),
                                null
                            )
                            isShowConfirmPassword = false
                            binding.etConfirmPassword.clearFocus()
                            binding.etConfirmPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.etConfirmPassword.setTextAppearance(this@ChangesPasswordActivity, R.style.editText_style)
                            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text!!.length)
                        } else {
                            binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_hide),
                                null
                            )
                            isShowConfirmPassword = true
                            binding.etConfirmPassword.clearFocus()
                            binding.etConfirmPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            binding.etConfirmPassword.setTextAppearance(
                                this@ChangesPasswordActivity,
                                R.style.editText_style
                            )
                            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text!!.length)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }
}
