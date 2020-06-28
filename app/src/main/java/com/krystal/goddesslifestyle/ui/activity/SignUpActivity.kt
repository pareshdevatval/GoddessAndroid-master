package com.krystal.goddesslifestyle.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.georeminder.utils.filePick.FilePickUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.krystal.goddesslifestyle.HowToUseAppActivity
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.databinding.ActivitySignUpBinding
import com.krystal.goddesslifestyle.databinding.DialogImageSelectionBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.shop.CartActivity
import com.krystal.goddesslifestyle.ui.shop.OrderReviewActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.AppUtils.setClickableString
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.SignUpViewModel
import javax.inject.Inject


class SignUpActivity : BaseActivity<SignUpViewModel>(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (v.id) {
                R.id.ivCameraIcon -> {
                    showImageSelectionDialog()
                }
                R.id.tvSelectCamera -> {
                    filePickUtils?.requestImageCamera(
                        AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
                R.id.tvSelectGallery -> {
                    filePickUtils?.requestImageGallery(
                        AppConstants.PICK_IMAGE_GALLERY_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
                R.id.tvSkip -> {
                    prefs.isSkipped = true
                    if (from.equals(AppConstants.FROM_CART, true)) {
                        onBackPressed()
                    } else {
                        startActivity(MainActivity.newInstance(this))
                        AppUtils.startFromRightToLeft(this)
                        finishAffinity()
                    }
                }
            }
        }
    }

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mImageSelectionDialog: BottomSheetDialog
    private lateinit var mViewModel: SignUpViewModel
    private var isShowPassword = false
    private var isShowConfirmPassword = false
    lateinit var editTextName: EditText
    lateinit var editTextEmail: EditText
    lateinit var editTextContact: EditText
    lateinit var editTextPassword: EditText
    lateinit var editTextConfirmPassword: EditText
    lateinit var editTextReferral: EditText
    private var filePickUtils: FilePickUtils? = null

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    var from: String = ""

    override fun getViewModel(): SignUpViewModel {
        mViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        return mViewModel
    }

    companion object {
        fun newInstance(context: Context, from: String = ""): Intent {
            val intent = Intent(context, SignUpActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_FROM, from)
            return intent
        }
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectSignUpActivity(this)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        intent?.let {
            it.getStringExtra(AppConstants.EXTRA_FROM)?.let { extraFrom ->
                from = extraFrom
            }
        }

        init()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    fun init() {
        filePickUtils = FilePickUtils(this, mOnFileChoose)
        binding.ivCameraIcon.setOnClickListener(this)
        binding.tvSkip.setOnClickListener(this)

        editTextName = findViewById(R.id.edtName)
        editTextEmail = findViewById(R.id.edtEmail)
        editTextContact = findViewById(R.id.edtContact)
        editTextPassword = findViewById(R.id.edtPassword)
        editTextConfirmPassword = findViewById(R.id.edtConfirmPassword)
        editTextReferral = findViewById(R.id.edtReferral)
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.setAppDatabase(appDatabase)

        mViewModel.getRegistrationResponse().observe(this, registrationResponseObserver)
        mViewModel.getUserSubscriptionResponse().observe(this, userSubscriptionObserver)

        binding.llBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivPlaceholder.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_user_image_placeholder
            )
        )
        binding.ivCameraIcon.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_camera_pink
            )
        )

        setClickableString(
            this,
            getString(R.string.lbl_sign_up_note),
            binding.tvByContinue,
            arrayOf(
                getString(R.string.lbl_terms_conditions),
                getString(R.string.lbl_privacy_policy)
            ),
            arrayOf(clickableTerms, clickablePrivacy)
        )

        setClickableString(
            this,
            getString(R.string.lbl_have_account_sign_in),
            binding.tvHaveAnAccount,
            arrayOf(getString(R.string.lbl_Sign_in)),
            arrayOf(clickableSignin)
        )


        binding.btnSignUp.setOnClickListener {

            if (editTextName.text.toString() == "") {
                AppUtils.showSnackBar(it, "Please enter name")
            } else if (editTextName.text.toString().length in 17..2) {
                AppUtils.showSnackBar(it, "Name character must in 3 to 16")
            } else if (editTextEmail.text.toString() == "") {
                AppUtils.showSnackBar(it, "Please enter email")
            } else if (!isEmailValid(editTextEmail.text.toString())) {
                AppUtils.showSnackBar(it, "Please enter valid email")
            } else if (editTextPassword.text.toString().trim() == "") {
                AppUtils.showSnackBar(it, "Please enter valid password")
            } else if (editTextPassword.text.toString().trim().length < 8) {
                AppUtils.showSnackBar(it, "Password length must be 8 characters")
            } else if (editTextConfirmPassword.text.toString().trim() == "") {
                AppUtils.showSnackBar(it, "Please enter confirm password")
            } else if (editTextConfirmPassword.text.toString()
                    .trim() != editTextPassword.text.toString().trim()
            ) {
                AppUtils.showSnackBar(it, "Password and confirm password must be same")
            } else {
                val requestParams = HashMap<String, String?>()
                requestParams[ApiContants.USERNAME] = editTextName.text.toString()
                requestParams[ApiContants.EMAIL] = editTextEmail.text.toString()
                requestParams[ApiContants.PASSWORD] = editTextPassword.text.toString()
                requestParams[ApiContants.USERTYPE] = "1"
                requestParams[ApiContants.USERSOCIALID] = ""
                requestParams[ApiContants.DEVICETYPE] = "android"
                requestParams[ApiContants.DEVICETOKEN] = prefs.firebaseToken!!
                requestParams[ApiContants.USERINVITATION] = editTextReferral.text.toString()
                requestParams[ApiContants.USERIMAGE] = ""
                mViewModel.callRegistrationApi(requestParams)
                Log.d("REQUEST", requestParams.toString())
            }
        }

        setHideShowPassword()

    }

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Glide.with(this@SignUpActivity).load(fileUri).into(binding.ivPlaceholder)
            mViewModel.userImages = fileUri

        }
    }

    private fun showImageSelectionDialog() {
        mImageSelectionDialog = BottomSheetDialog(this)
        val dialogImageSelectionBinding = DialogImageSelectionBinding.inflate(layoutInflater)
        mImageSelectionDialog.setContentView(dialogImageSelectionBinding.root)

        dialogImageSelectionBinding.tvSelectCamera.setOnClickListener(this)
        dialogImageSelectionBinding.tvSelectGallery.setOnClickListener(this)
        mImageSelectionDialog.show()

    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setHideShowPassword() {
        binding.edtPassword.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                val DRAWABLE_RIGHT = 2

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.edtPassword.right - binding.edtPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isShowPassword) {
                            binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                ContextCompat.getDrawable(
                                    this@SignUpActivity, R.drawable.ic_password_show
                                ),
                                null
                            )
                            isShowPassword = false
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            TextViewCompat.setTextAppearance(
                                binding.edtPassword,
                                R.style.editText_style
                            )
                            binding.edtPassword.setSelection(binding.edtPassword.text!!.length)
                        } else {
                            binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                ContextCompat.getDrawable(
                                    this@SignUpActivity, R.drawable.ic_password_hide
                                ),
                                null
                            )
                            isShowPassword = true
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            TextViewCompat.setTextAppearance(
                                binding.edtPassword,
                                R.style.editText_style
                            )
                            binding.edtPassword.setSelection(binding.edtPassword.text!!.length)

                        }
                        return true
                    }
                }
                return false
            }
        })

        binding.edtConfirmPassword.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                val DRAWABLE_RIGHT = 2

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.edtPassword.right - binding.edtConfirmPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isShowConfirmPassword) {
                            binding.edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                ContextCompat.getDrawable(
                                    this@SignUpActivity,
                                    R.drawable.ic_password_show
                                ),
                                null
                            )
                            isShowConfirmPassword = false
                            binding.edtConfirmPassword.clearFocus()
                            binding.edtConfirmPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            TextViewCompat.setTextAppearance(
                                binding.edtConfirmPassword,
                                R.style.editText_style
                            )
                            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.text!!.length)
                        } else {
                            binding.edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                ContextCompat.getDrawable(
                                    this@SignUpActivity, R.drawable.ic_password_hide
                                ),
                                null
                            )
                            isShowConfirmPassword = true
                            binding.edtConfirmPassword.clearFocus()
                            binding.edtConfirmPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            TextViewCompat.setTextAppearance(
                                binding.edtConfirmPassword,
                                R.style.editText_style
                            )
                            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.text!!.length)

                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private val registrationResponseObserver = Observer<LoginResponse> { resoponse ->
        if (resoponse.status) {

            prefs.userDataModel = resoponse
            prefs.isLoggedIn = true

            mViewModel.callGetUserSubscriptionApi()


        } else {
            AppUtils.showSnackBar(binding.btnSignUp, resoponse.message)
        }
    }

    private val userSubscriptionObserver = Observer<UserSubscriptionResponse> {
        if (it.status) {
            it.result?.let {
                appDatabase.userSubscriptionDao().insert(it)
            }
        }
        startActivity(HowToUseAppActivity.newInstance(this, from))
        AppUtils.startFromRightToLeft(this)
        finishAffinity()

        /* if(from.equals(AppConstants.FROM_CART, true)) {
             startActivity(OrderReviewActivity.newInstance(this))
             AppUtils.startFromRightToLeft(this)
             finish()
         } else {
             startActivity(MainActivity.newInstance(this))
             AppUtils.startFromRightToLeft(this)
             finishAffinity()
         }*/
    }

    /**
     * Have an account? SignIn spannable and click
     */
    private val clickableSignin: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            if (from.equals(AppConstants.FROM_CART, true)) {
                startActivity(LoginActivity.newInstance(this@SignUpActivity))
                GoddessAnimations.finishFromRightToLeft(this@SignUpActivity)
                finish()
            } else {
                onBackPressed()
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.color_bg_pink)
        }
    }

    /**
     * Terms and condition or Privacy Policy spannable and click
     */

    private val clickableTerms: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            startActivity(
                WebViewActivity.newInstance(
                    this@SignUpActivity,
                    getString(R.string.lbl_terms_conditions),
                    AppConstants.TERMS_URL
                )
            )

        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.color_bg_pink)
        }
    }

    private val clickablePrivacy: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            startActivity(
                WebViewActivity.newInstance(
                    this@SignUpActivity,
                    getString(R.string.lbl_privacy_policy),
                    AppConstants.PRIVACY_URL
                )
            )
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.color_bg_pink)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        filePickUtils?.onActivityResult(requestCode, resultCode, intent)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePickUtils?.onRequestPermissionsResult(
            requestCode,
            permissions as Array<String>,
            grantResults
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromRightToLeft(this)
    }
}
