package com.krystal.goddesslifestyle.ui.community

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.CommunityViewpagerImagesAdapterWrapContent
import com.krystal.goddesslifestyle.adapter.ViewpagerImagesAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.databinding.FragmentGoddessCommunityBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.GoddessCommunityViewModel
import javax.inject.Inject

/**
 * Created by imobdev on 30/3/20
 */
class GoddessCommunityFragment : BaseFragment<GoddessCommunityViewModel>() {
    private lateinit var mViewModel: GoddessCommunityViewModel
    private lateinit var binding: FragmentGoddessCommunityBinding
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): GoddessCommunityFragment {
            val bundle = Bundle()
            val fragment = GoddessCommunityFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val requestsComponent: NetworkLocalComponent =
            DaggerNetworkLocalComponent.builder().networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent()).build()
        requestsComponent.injectGoddessCommunityFragment(this)
        binding = FragmentGoddessCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setInjectable(apiService, prefs)
        init()
        setObserver()
        binding.goddessCommmunity=this
        mViewModel.callGetGoddessCommunityCount()
    }

    private fun init(){
        (activity as MainActivity).setActivityAnimatedBackground()
        setToolbarTitle(R.string.title_goddess_community)
        setToolbarColor(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, R.color.violet)
        }

        var imagesList= arrayListOf<String>()
        imagesList.add(R.drawable.teacher_of_the_month.toString())
        //imagesList.add(R.drawable.teacher_of_the_month.toString())
        //imagesList.add(R.drawable.teacher_of_the_month.toString())

        val adapter = CommunityViewpagerImagesAdapterWrapContent(context!!, imagesList)
        binding.viewPager.adapter = adapter
        binding.vpIndicator.setViewPager(binding.viewPager)
       /* setToolbarRightIcon(
            R.drawable.ic_search,
            object : BaseActivity.ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    //showSearchBar(true)
                }
            })*/
    }
    override fun getViewModel(): GoddessCommunityViewModel {
        mViewModel = ViewModelProvider(this).get(GoddessCommunityViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    fun openCommunityDetails() {
        context?.let {
            val subscriptionStatus = AppUtils.getUserSubscription(it)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(context)
            } else {
                if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                    //AppUtils.startSubscriptionActivity(context)
                    AppUtils.startSubscriptionActivity(context, true)
                } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                    if ((activity as MainActivity).getCurrentFragment() != null) {
                        if ((activity as MainActivity).getCurrentFragment() !is GoddessCommunityOpinionsFragment) {
                            (activity as MainActivity).replaceFragment(R.id.frame_container, GoddessCommunityOpinionsFragment.newInstance(), false)
                        }
                    } else {
                        (activity as MainActivity).replaceFragment(R.id.frame_container, GoddessCommunityOpinionsFragment.newInstance(), false)
                    }
                }
            }
        }
    }

    private fun setObserver() {
        mViewModel.getGoddessCommunityCountResponse()
            .observe({ this.lifecycle }, {
                if (it.status) {
                    if (it.countData.total_community_opinions_count > 0) {
                        //binding.clOpinionView.visibility = View.VISIBLE
                        binding.clOpinionView.visibility = View.GONE
                        binding.tvCount.text =
                            it.countData.total_community_opinions_count.toString()
                    } else {
                        binding.clOpinionView.visibility = View.GONE
                    }
                } else {
                    binding.clOpinionView.visibility = View.GONE
                }
            })
    }
}