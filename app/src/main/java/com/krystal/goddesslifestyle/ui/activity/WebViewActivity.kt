package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityWebviewBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.viewmodels.WebViewModel
import javax.inject.Inject

class WebViewActivity : BaseActivity<WebViewModel>() {

    private lateinit var binding: ActivityWebviewBinding
    private lateinit var mViewModel: WebViewModel

    lateinit var title: String
    lateinit var url: String

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
        fun newInstance(context: Context, title: String, url: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(AppConstants.TITLE, title)
            intent.putExtra(AppConstants.URL, url)
            return intent
        }
    }

    override fun getViewModel(): WebViewModel {
        mViewModel = ViewModelProvider(this).get(WebViewModel::class.java)
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
        requestsComponent.injectWebViewActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        intent?.let {
            title = intent.getStringExtra(AppConstants.TITLE)
            url = intent.getStringExtra(AppConstants.URL)

            init()
        }
    }

    fun init() {


        setToolBar(R.color.pink)
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://$url"

        showProgress()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(url)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideProgress()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                hideProgress()
            }
        }
    }

    private fun setToolBar(bgColor: Int) {
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
