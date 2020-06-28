package com.krystal.goddesslifestyle.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.databinding.AppToolbarBinding
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.BillingManager


@SuppressLint("Registered")


/**
 * A Base class for All activities
 * Created by Bhargav Thanki on 24/1/21.
 */
abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

    private lateinit var mViewModel: T
    private var progressDialog: Dialog? = null

    //lateinit var toolbarBinding: com.imobdev.goddesslifestyle.databinding.AppToolbarBinding
    private var errorSnackbar: Snackbar? = null
    private val errorClickListener = View.OnClickListener { internetErrorRetryClicked() }
    protected var lastClickTime: Long = 0
    lateinit var toolbarBinding: AppToolbarBinding

    /*val toolbarBinding by lazy {
        val toolbar = findViewById<Toolbar>(R.id.toolbarLayout)
        setSupportActionBar(toolbar)
        // getting toolbar binding
        DataBindingUtil.getBinding<AppToolbarBinding>(toolbar) as AppToolbarBinding
    }*/

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = getViewModel()

        mViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) showError(errorMessage) else hideError()
        })

        mViewModel.loadingVisibility.observe(this, Observer { loadingVisibility ->
            loadingVisibility?.let { if (loadingVisibility) showProgress() else hideProgress() }
        })

        mViewModel.apiErrorMessage.observe(this, Observer { apiError ->
            apiError?.let { AppUtils.showSnackBar(getRootView(), it) }
        })

        // Billing APIs are all handled in the this lifecycle observer.
        billingManager = (application as GoddessLifeStyleApp).getBillingManager()
        lifecycle.addObserver(billingManager)

    }

    private fun getRootView(): View {
        val contentViewGroup = findViewById<View>(android.R.id.content) as ViewGroup
        var rootView = contentViewGroup.getChildAt(0)
        if (rootView == null) rootView = window.decorView.rootView
        return rootView!!
    }

    // initializing application toolbar
    fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarLayout)
        //setSupportActionBar(toolbar)
        // getting toolbar binding
        toolbarBinding = DataBindingUtil.getBinding<AppToolbarBinding>(toolbar) as AppToolbarBinding
    }

    private fun showError(@StringRes errorMessage: Int) {
        errorSnackbar = Snackbar.make(getRootView(), errorMessage, Snackbar.LENGTH_LONG)
        //errorSnackbar?.setAction(R.string.action_retry, errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError() {
        errorSnackbar?.dismiss()
    }

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract fun getViewModel(): T

    abstract fun internetErrorRetryClicked()

    fun setToolbarColor(color: Int) {
        if (::toolbarBinding.isInitialized) {
            toolbarBinding.toolbar.setBackgroundColor(ContextCompat.getColor(this, color))
        }
    }

    fun hideToolbar() {
        toolbarBinding.toolbar.visibility = View.GONE
    }

    fun showToolbar() {
        toolbarBinding.toolbar.visibility = View.VISIBLE
    }

    // toolabr title
    fun setToolbarTitle(title: Int) {
        initToolbar()
        toolbarBinding.tvToolbarTitle.text = resources.getString(title)
    }

    // toolabr title
    fun setToolbarRightText(title: Int) {
        initToolbar()
        toolbarBinding.tvToolbarRight.visibility = View.VISIBLE
        toolbarBinding.tvToolbarRight.text = resources.getString(title)
    }

    fun hideToolbarRightText() {
        toolbarBinding.tvToolbarRight.visibility = View.GONE
    }

    // toolabr title
    fun setToolbarTitle(title: String) {
        initToolbar()
        toolbarBinding.tvToolbarTitle.text = title
    }

    // set icon of toolbar left icon
    fun setToolbarLeftIcon(
        resourceId: Int,
        toolbarLeftClickListener: ToolbarLeftImageClickListener
    ) {
        initToolbar()
        toolbarBinding.ivToolbarLeft.setImageResource(resourceId)
        toolbarBinding.llToolbarLeft.visibility = View.VISIBLE
        toolbarBinding.llToolbarLeft.setOnClickListener { toolbarLeftClickListener.onLeftImageClicked() }
    }

    // set toolbar right icon and implement its click
    fun setToolbarRightIcon(
        resourceId: Int,
        toolbarRightClickListener: ToolbarRightImageClickListener
    ) {
        toolbarBinding.ivToolbarRight.setImageResource(resourceId)
        toolbarBinding.llToolbarRight.visibility = View.VISIBLE

        toolbarBinding.llToolbarRight.setOnClickListener { toolbarRightClickListener.onRightImageClicked() }
        //toolbarBinding.llToolbarRight.setOnClickListener { toolbarRightClickListener.onRightImageClicked() }
    }

    fun setToolbarRight1Icon(
        resourceId: Int,
        toolbarRightClickListener: ToolbarRight1ImageClickListener
    ) {
        toolbarBinding.ivToolbarRight1.setImageResource(resourceId)
        toolbarBinding.llToolbarRight1.visibility = View.VISIBLE

        toolbarBinding.llToolbarRight1.setOnClickListener { toolbarRightClickListener.onRight1ImageClicked() }
        //toolbarBinding.llToolbarRight.setOnClickListener { toolbarRightClickListener.onRightImageClicked() }
    }

    fun getToolbarRightIcon(): View {
        return toolbarBinding.tvDummy

    }

    // hide toolbar right icon when not needed
    fun hideToolbarRightIcon() {
        toolbarBinding.llToolbarRight.visibility = View.GONE
    }

    // hide toolbar right icon when not needed
    fun showToolbarRightIcon() {
        toolbarBinding.llToolbarRight.visibility = View.VISIBLE
    }

    // hide toolbar left icon when not needed
    fun hideToolbarLeftIcon() {
        toolbarBinding.llToolbarLeft.visibility = View.GONE
    }

    @SuppressLint("InflateParams")
    fun showProgress(showText: Boolean = false) {
        if (progressDialog == null) {
            progressDialog = Dialog(this)
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        // inflating and setting view of custom dialog
        val view = LayoutInflater.from(this).inflate(R.layout.app_loading_dialog, null, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)

        val tvInitializing = view.findViewById<TextView>(R.id.tvInitializing)

        if (showText) {
            tvInitializing.visibility = View.VISIBLE
        } else {
            tvInitializing.visibility = View.GONE
        }

        // applying rotate animation
        //imageView.pivotX = 0.5f
        ///imageView.pivotY = 0.5f
        /*ObjectAnimator.ofFloat(imageView, View.ROTATION, 360f, 0f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 1500
            interpolator = LinearInterpolator()
            start()
        }*/
        Glide.with(this)
            .asGif()
            .load(R.drawable.gif_api)
            //.fitCenter()
            .into(imageView)
        progressDialog!!.setContentView(view)

        // setting background of dialog as transparent
        val window = progressDialog!!.window
        window?.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        // preventing outside touch and setting cancelable false
        progressDialog!!.setCancelable(false)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()
    }


    fun showConfigProgress() {
        if (progressDialog == null) {
            progressDialog = Dialog(this)
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        // inflating and setting view of custom dialog
        val view = LayoutInflater.from(this).inflate(R.layout.app_loading_dialog, null, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val tvInitializing = view.findViewById<TextView>(R.id.tvInitializing)
        tvInitializing.visibility = View.VISIBLE
        // applying rotate animation
        //imageView.pivotX = 0.5f
        ///imageView.pivotY = 0.5f
        /*ObjectAnimator.ofFloat(imageView, View.ROTATION, 360f, 0f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 1500
            interpolator = LinearInterpolator()
            start()
        }*/
        Glide.with(this)
            .asGif()
            .load(R.drawable.gif_config)
            .fitCenter()
            .into(imageView)

        progressDialog!!.setContentView(view)

        // setting background of dialog as transparent
        val window = progressDialog!!.window
        window?.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        // preventing outside touch and setting cancelable false
        progressDialog!!.setCancelable(false)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()
    }


    fun hideProgress() {
        progressDialog?.run {
            if (isShowing) {
                dismiss()
            }
        }
    }

    /* [START] Check if an active internet connection is present or not*/
    /* return boolen value true or false */
    fun isInternetAvailable(): Boolean {
        // getting Connectivity service as ConnectivtyManager
        return AppUtils.hasInternet(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            progressDialog = null
        }
    }

    // interface class for toolbar right icon click
    interface ToolbarRightImageClickListener {
        fun onRightImageClicked()
    }

    // interface class for toolbar right icon click
    interface ToolbarRight1ImageClickListener {
        fun onRight1ImageClicked()
    }

    // interface class for toolbar left icon click
    interface ToolbarLeftImageClickListener {
        fun onLeftImageClicked()
    }

    private fun getApp() = (application as GoddessLifeStyleApp)

    fun getAppComponent() = getApp().getAppComponent()

    fun getLocalDataComponent() = getApp().getLocalDataComponent()

    fun getNetworkComponent() = getApp().getNetworkComponent()


    /* Accepts
    * containerId : Frame Layout id to replace the fragment. Different activities may have different id for
    * the container. Hence this id is needed
    * fragment : Fragment to replace
    * addToBackStack : Want to add to backstack or not
    * */
    fun replaceFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        /* We are showing the shared elements transitions from API version 21
        * And if any fragment has not any shared elements.
        * Then also we will not show custom animations to change mInputCharacter fragment.
        * Becuase then our app will have mInputCharacter mix of animations. And we do not want that
        * We want to use single type of animations throughout the app
        * and Hence showing custom animations of below API version 21(Where we can not use shared element transitions)
        * */
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        *//*setting mInputCharacter custom animation to fragment transaction
        new fragment will come from right to left and
        an older fragment will remove from left*//*
        fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
        R.anim.slide_left_in, R.anim.slide_right_out)
        }*/
        // replacing mInputCharacter current fragment
        fragmentTransaction.replace(containerId, fragment)
        // Add current fragment to backstack or not
        if (addToBackStack)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        // commiting the transaction
        fragmentTransaction.commitNow()
    }

    fun addFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (fragment.isAdded) {
            val currentFragment = supportFragmentManager.findFragmentById(containerId)
            fragmentTransaction.show(fragment)
            fragmentTransaction.hide(currentFragment!!)
            fragmentTransaction.commit()
            return
        }
        /* We are showing the shared elements transitions from API version 21
        * And if any fragment has not any shared elements.
        * Then also we will not show custom animations to change mInputCharacter fragment.
        * Becuase then our app will have mInputCharacter mix of animations. And we do not want that
        * We want to use single type of animations throughout the app
        * and Hence showing custom animations of below API version 21(Where we can not use shared element transitions)
        * */
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        *//*setting mInputCharacter custom animation to fragment transaction
        new fragment will come from right to left and
        an older fragment will remove from left*//*
        fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
        R.anim.slide_left_in, R.anim.slide_right_out)
        }*/
        // replacing mInputCharacter current fragment
        fragmentTransaction.add(containerId, fragment)
        // Add current fragment to backstack or not
        if (addToBackStack)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        // commiting the transaction
        fragmentTransaction.commit()
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.frame_container)
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    // show Search bar
    fun showSearchBar(bgColor: Int = R.color.green) {
        toolbarBinding.searchBarView.setBackgroundColor(ContextCompat.getColor(this, bgColor))
        toolbarBinding.searchBarView.visibility = View.VISIBLE
        val shake = AnimationUtils.loadAnimation(
            this,
            R.anim.shake
        )

        toolbarBinding.searchBarView.startAnimation(shake)
        toolbarBinding.ivToolbarRight.visibility = View.GONE
        toolbarBinding.edtSearch.requestFocus()

        AppUtils.showKeyboard(this, toolbarBinding.edtSearch)
    }

    fun showCartView() {
        toolbarBinding.llToolbarCart.visibility = View.VISIBLE
    }

    // hide  Search bar
    fun hideSearchBar() {
        toolbarBinding.searchBarView.visibility = View.GONE
        val shake = AnimationUtils.loadAnimation(
            this,
            R.anim.return_shake
        )

        toolbarBinding.searchBarView.startAnimation(shake)

        toolbarBinding.ivToolbarRight.visibility = View.VISIBLE
        toolbarBinding.edtSearch.clearFocus()
        toolbarBinding.edtSearch.setText("")
        AppUtils.hideKeyboard(this)
    }

    fun getEditTextView(): EditText {
        return toolbarBinding.edtSearch
    }
}