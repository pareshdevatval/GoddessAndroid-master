package com.krystal.goddesslifestyle.ui.recipe

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.viewmodels.HowToAddRecipeViewModel
import com.krystal.goddesslifestyle.R

import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent

class HowToAddRecipeActivity : BaseActivity<HowToAddRecipeViewModel>() {
    private lateinit var mViewModel: HowToAddRecipeViewModel
    private lateinit var binding: com.krystal.goddesslifestyle.databinding.ActivityHowToAddRecipeBinding

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, HowToAddRecipeActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectHowToAddRecipeActivity(this)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_how_to_add_recipe)
        super.onCreate(savedInstanceState)
        init()
    }


    private fun init(){
        setToolbarTitle(getString(R.string.title_how_to_add_own))
        setToolbarColor(R.color.green)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }

        setToolbarLeftIcon(
            R.drawable.ic_back,
            object : ToolbarLeftImageClickListener {
                override fun onLeftImageClicked() {
                    onBackPressed()
                }
            })
    }


    override fun getViewModel(): HowToAddRecipeViewModel {
        mViewModel = ViewModelProvider(this).get(HowToAddRecipeViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }


}
