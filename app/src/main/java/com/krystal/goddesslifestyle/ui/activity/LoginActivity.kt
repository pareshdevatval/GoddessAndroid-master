package com.krystal.goddesslifestyle.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.LoginResponse
import com.krystal.goddesslifestyle.data.response.UserSubscriptionResponse
import com.krystal.goddesslifestyle.databinding.ActivityLoginBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.shop.OrderReviewActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.LoginViewModel
import com.stripe.android.exception.APIException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class LoginActivity : BaseActivity<LoginViewModel>(), View.OnClickListener {

    val PERMISSION_REQ_EXTERNAL_STORAGE = 11

    private val RC_SIGN_IN = 81
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mViewModel: LoginViewModel
    lateinit var editTextEmail: EditText
    lateinit var editTextPassword: EditText

    private var callbackManager: CallbackManager? = null
    private var isShowPassword = false

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

    var mGoogleSignInClient: GoogleSignInClient? = null

    var selectedSocialLogin = ""
    val FACEBOOK = "Facebook"
    var GOOGLE = "Google"

    companion object {
        fun newInstance(context: Context, from: String = ""): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_FROM, from)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()

        requestsComponent.injectLoginActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        intent?.let {
            it.getStringExtra(AppConstants.EXTRA_FROM)?.let { extraFrom ->
                from = extraFrom
            }
        }

        init()
        binding.activity = this
        editTextEmail = findViewById(R.id.edtEmail)
        editTextPassword = findViewById(R.id.edtPassword)
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.setAppDatabase(appDatabase)

        mViewModel.getLoginResponse().observe(this, loginResponseObserver)
        mViewModel.getUserSubscriptionResponse().observe(this, userSubscriptionObserver)


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //binding.btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE)
        binding.btnGoogleSignIn.setOnClickListener(this)


        binding.btnLogin.setOnClickListener {

            if (editTextEmail.text.toString() == "") {
                AppUtils.showSnackBar(it, "Please enter email")
            } else if (!isEmailValid(editTextEmail.text.toString())) {
                AppUtils.showSnackBar(it, "Please enter valid email")
            } else if (editTextPassword.text.toString() == "") {
                AppUtils.showSnackBar(it, "Please enter password")
            } else if (editTextPassword.text.toString().length < 6) {
                AppUtils.showSnackBar(it, "Password length must be 6 character")
            } else {

                val requestParams = HashMap<String, String>()
                requestParams[ApiContants.EMAIL] = editTextEmail.text.toString()
                requestParams[ApiContants.PASSWORD] = editTextPassword.text.toString()
                requestParams[ApiContants.USERTYPE] = "1"
                requestParams[ApiContants.USERSOCIALID] = ""
                requestParams[ApiContants.DEVICETYPE] = "android"
                requestParams[ApiContants.DEVICETOKEN] = prefs.firebaseToken!!
                mViewModel.callLoginApi(requestParams)
            }
        }

        if (prefs.rememberMe) {
            binding.edtEmail.setText(prefs.userName)
            binding.edtPassword.setText(prefs.password)
            binding.chbRememberMe.isChecked = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    fun init() {
        FacebookSdk.sdkInitialize(applicationContext)

        AppUtils.setClickableString(
            this,
            getString(R.string.lbl_dont_have_account),
            binding.tvHaveAnAccount,
            arrayOf(getString(R.string.lbl_Sign_up)),
            arrayOf(clickableSignin)
        )
        loginWithFB()

        setHideShowPassword()
    }

    private fun loginWithFB() {
        binding.btnFacebook.setOnClickListener(View.OnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // ask for permission
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    showRationaleDialogForStorage(FACEBOOK)
                } else {
                    // No explanation needed, we can request the permission.
                    selectedSocialLogin = FACEBOOK
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQ_EXTERNAL_STORAGE
                    )
                }

            } else {
                // permission already granted
                performLoginWithFb()
            }
        })
    }

    private fun showRationaleDialogForStorage(from: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Permissions")
        builder.setMessage(
            "Storage permission is required to download the photo from your " + from + " account." +
                    "Please allow the permission"
        )

        builder.setPositiveButton("OK") { dialog, p1 ->
            dialog.dismiss()
            selectedSocialLogin = from
            ActivityCompat.requestPermissions(
                this@LoginActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQ_EXTERNAL_STORAGE
            )
        }

        builder.setPositiveButton("CANCEL") { dialog, p1 ->
            dialog.dismiss()
            performLoginWithFb()
        }
    }

    private fun performLoginWithFb() {
        // Login
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            Arrays.asList("public_profile", "email", "user_birthday", "user_friends")
        )
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    val request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken()
                    ) { `object`, response ->

                        val email = `object`.optString("email")
                        val birthday = `object`.optString("birthday")
                        val name = `object`.optString("name")
                        val image_url =
                            "https://graph.facebook.com/" + `object`.optString("id") + "/picture?type=large"
                        AppUtils.logE(email)

                        AppUtils.logE(name)
                        AppUtils.logE(image_url)

                        if (image_url.isNotBlank() && ContextCompat.checkSelfPermission(
                                this@LoginActivity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {

                            showProgress()
                            Glide.with(this@LoginActivity)
                                .asBitmap()
                                .load(image_url)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        val filePath = AppUtils.createImageFile(
                                            this@LoginActivity,
                                            resource,
                                            from
                                        )
                                        mViewModel.userProfileImage = filePath

                                        val requestParams = HashMap<String, String>()
                                        requestParams[ApiContants.USERNAME] = name
                                        requestParams[ApiContants.EMAIL] = email
                                        requestParams[ApiContants.PASSWORD] = ""
                                        requestParams[ApiContants.USERTYPE] = "2"
                                        requestParams[ApiContants.USERSOCIALID] =
                                            `object`.optString("id")
                                        requestParams[ApiContants.DEVICETYPE] = "android"
                                        requestParams[ApiContants.DEVICETOKEN] =
                                            prefs.firebaseToken!!
                                        requestParams[ApiContants.USERINVITATION] = ""
                                        requestParams[ApiContants.USERIMAGE] = ""
                                        mViewModel.callLoginApi(requestParams, true)
                                    }
                                })
                        } else {

                            mViewModel.userProfileImage = ""

                            val requestParams = HashMap<String, String>()
                            requestParams[ApiContants.USERNAME] = name
                            requestParams[ApiContants.EMAIL] = email
                            requestParams[ApiContants.USERMOBILE] = ""
                            requestParams[ApiContants.PASSWORD] = ""
                            requestParams[ApiContants.USERTYPE] = "2"
                            requestParams[ApiContants.USERSOCIALID] = `object`.optString("id")
                            requestParams[ApiContants.DEVICETYPE] = "android"
                            requestParams[ApiContants.DEVICETOKEN] = prefs.firebaseToken!!
                            requestParams[ApiContants.USERINVITATION] = ""
                            requestParams[ApiContants.USERIMAGE] = ""
                            mViewModel.callLoginApi(requestParams, true)
                        }

                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,gender,birthday")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    AppUtils.logE("Facebook onCancel.")

                }

                override fun onError(error: FacebookException) {
                    AppUtils.logE("Facebook onError.")
                }
            })
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
                                resources.getDrawable(R.drawable.ic_password_show),
                                null
                            )
                            isShowPassword = false
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtPassword.setTextAppearance(
                                this@LoginActivity,
                                R.style.editText_style
                            )
                            binding.edtPassword.setSelection(binding.edtPassword.text!!.length)
                        } else {
                            binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                resources.getDrawable(R.drawable.ic_password_hide),
                                null
                            )
                            isShowPassword = true
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                            binding.edtPassword.setTextAppearance(
                                this@LoginActivity,
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        } else {
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                Log.e("GOOGLE_SIGN_IN", "Account Id : " + it.id)
                Log.e("GOOGLE_SIGN_IN", "Account Name : " + it.displayName)
                Log.e("GOOGLE_SIGN_IN", "Account Email : " + it.email)
                Log.e("GOOGLE_SIGN_IN", "Account Photo : " + it.photoUrl)


                val requestParams = HashMap<String, String>()
                if (it.displayName != null) {
                    requestParams[ApiContants.USERNAME] = it.displayName!!
                } else {
                    requestParams[ApiContants.USERNAME] = ""
                }

                if (it.email != null) {
                    requestParams[ApiContants.EMAIL] = it.email!!
                } else {
                    requestParams[ApiContants.EMAIL] = ""
                }

                val photoUrl = it.photoUrl
                val userName = it.displayName
                val userEmail = it.email
                val accountId = it.id

                if (photoUrl != null && photoUrl.toString()
                        .isNotBlank() && ContextCompat.checkSelfPermission(
                        this@LoginActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {

                    showProgress()
                    Glide.with(this@LoginActivity)
                        .asBitmap()
                        .load(photoUrl)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                val filePath =
                                    AppUtils.createImageFile(this@LoginActivity, resource, from)
                                mViewModel.userProfileImage = filePath

                                val requestParams = HashMap<String, String>()
                                requestParams[ApiContants.USERNAME] = userName ?: ""
                                requestParams[ApiContants.EMAIL] = userEmail ?: ""
                                requestParams[ApiContants.PASSWORD] = ""
                                requestParams[ApiContants.USERTYPE] = "3"
                                requestParams[ApiContants.USERSOCIALID] = accountId ?: ""
                                requestParams[ApiContants.DEVICETYPE] = "android"
                                requestParams[ApiContants.DEVICETOKEN] = prefs.firebaseToken!!
                                requestParams[ApiContants.USERINVITATION] = ""
                                requestParams[ApiContants.USERIMAGE] = ""
                                mViewModel.callLoginApi(requestParams, true)
                            }
                        })
                } else {

                    mViewModel.userProfileImage = ""

                    val requestParams = HashMap<String, String>()
                    requestParams[ApiContants.USERNAME] = userName ?: ""
                    requestParams[ApiContants.EMAIL] = userEmail ?: ""
                    requestParams[ApiContants.USERMOBILE] = ""
                    requestParams[ApiContants.PASSWORD] = ""
                    requestParams[ApiContants.USERTYPE] = "3"
                    requestParams[ApiContants.USERSOCIALID] = accountId ?: ""
                    requestParams[ApiContants.DEVICETYPE] = "android"
                    requestParams[ApiContants.DEVICETOKEN] = prefs.firebaseToken!!
                    requestParams[ApiContants.USERINVITATION] = ""
                    requestParams[ApiContants.USERIMAGE] = ""
                    mViewModel.callLoginApi(requestParams, true)
                }
            }

            // signed in successfully
        } catch (e: APIException) {
            Log.e("GOOGLE_SIGN_IN", "" + e.message)
        }
    }

    /**
     * Have an account? SignUp spannable and click
     */
    private val clickableSignin: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {

            startActivity(SignUpActivity.newInstance(this@LoginActivity, from))
            AppUtils.startFromRightToLeft(this@LoginActivity)
            if (from.equals(AppConstants.FROM_CART, true)) {
                finish()
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(this@LoginActivity, R.color.color_bg_pink)
        }
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun getViewModel(): LoginViewModel {
        mViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    fun startForgetPasswordActivity() {
        startActivity(ForgetPasswordActivity.newInstance(this))
        AppUtils.startFromRightToLeft(this)
    }

    //@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private val loginResponseObserver = Observer<LoginResponse> { resoponse ->
        if (resoponse.status) {
            prefs.isLoggedIn = true
            prefs.userDataModel = resoponse
            if (binding.chbRememberMe.isChecked) {
                prefs.rememberMe = true
                prefs.userName = binding.edtEmail.text.toString().trim()
                prefs.password = binding.edtPassword.text.toString().trim()
            } else {
                prefs.rememberMe = false
                prefs.userName = ""
                prefs.password = ""
            }
            mViewModel.callGetUserSubscriptionApi()

        } else {
            AppUtils.showSnackBar(binding.btnLogin, resoponse.message)
        }
    }

    private val userSubscriptionObserver = Observer<UserSubscriptionResponse> {
        if (it.status) {
            it.result?.let {
                appDatabase.userSubscriptionDao().insert(it)
            }
        }

        if (from.equals(AppConstants.FROM_CART, true)) {
            startActivity(OrderReviewActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finish()
        } else {
            startActivity(MainActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finishAffinity()
        }
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (v.id) {
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
                R.id.btn_google_sign_in -> {
                    //googleSignIn()
                    if (!AppUtils.hasInternet(this)) {
                        AppUtils.showToast(this, "No Internet connection!")
                        return
                    }
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        // ask for permission
                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                            showRationaleDialogForStorage(GOOGLE)
                        } else {
                            // No explanation needed, we can request the permission.
                            selectedSocialLogin = GOOGLE
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PERMISSION_REQ_EXTERNAL_STORAGE
                            )
                        }

                    } else {
                        // permission already granted
                        googleSignIn()
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        signInIntent?.let {
            startActivityForResult(it, RC_SIGN_IN)
        }
    }

    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val signedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signedInAccount == null) {
            Log.e("GOOGLE_SIGN_IN", "User is not logged in via Google")
        } else {
            Log.e("GOOGLE_SIGN_IN", "User is logged in via Google")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromRightToLeft(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (selectedSocialLogin == FACEBOOK) {
                    performLoginWithFb()
                } else if (selectedSocialLogin == GOOGLE) {
                    //performLoginWithFb()
                    googleSignIn()
                }
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                if (selectedSocialLogin == FACEBOOK) {
                    performLoginWithFb()
                } else if (selectedSocialLogin == GOOGLE) {
                    //performLoginWithFb()
                    googleSignIn()
                }
            }
            return
        }
    }
}
