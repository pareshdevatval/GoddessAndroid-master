package com.krystal.goddesslifestyle.ui.video_library

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.VideoListingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.VideoCategoriesResponse
import com.krystal.goddesslifestyle.databinding.FragmentVideoListingBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.VideoListingViewModel
import javax.inject.Inject

/**
 * Created by Bhargav Thanki on 20 March,2020.
 */
class VideoListingFragment: BaseFragment<VideoListingViewModel>(),
    BaseBindingAdapter.ItemClickListener<VideoCategoriesResponse.VideoCategory> {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): VideoListingFragment {
            val bundle = Bundle()
            val fragment = VideoListingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: VideoListingViewModel
    private lateinit var binding: FragmentVideoListingBinding

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectVideoListingFragment(this)

        binding = FragmentVideoListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        viewModel.setInjectable(apiService, prefs)

        viewModel.getVideoCategoriesResponse().observe(viewLifecycleOwner, videoCategoriesResponseObserver)
        viewModel.callVideoCategoriesApi()
    }

    fun init() {
        (activity as MainActivity).changeBgColor(R.color.pink)

        // toolbar title
        setToolbarTitle(R.string.title_video_library)

        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, R.color.colorAccent)
        }

        val staggeredLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredLayoutManager

        val adapter = VideoListingAdapter()
        binding.recyclerView.adapter = adapter
        adapter.itemClickListener = this
    }

    override fun getViewModel(): VideoListingViewModel {
        viewModel = ViewModelProvider(this).get(VideoListingViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private val videoCategoriesResponseObserver = Observer<VideoCategoriesResponse>() {
        if(it.status) {
            val videoCategories = it.videoCategories
            (binding.recyclerView.adapter as VideoListingAdapter).setItem(videoCategories as ArrayList<VideoCategoriesResponse.VideoCategory>)
        }
    }

    override fun onItemClick(view: View, data: VideoCategoriesResponse.VideoCategory, position: Int) {
        context?.let {
            // subscription check
            val subscriptionStatus = AppUtils.getUserSubscription(it)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(context)
            } else {
                val intent = VideoDetailsListActivity.newInstance(it)

                if(data.subCategories != null && data.subCategories!!.isNotEmpty()) {
                    // passing video category data class in the intent
                    // data class is Pacelable
                    intent.putParcelableArrayListExtra(AppConstants.EXTRA_SUB_CATEGORIES, data.subCategories
                            as java.util.ArrayList<VideoCategoriesResponse.VideoCategory.VideoSubCategory>)
                    intent.putExtra(AppConstants.EXTRA_CAT_NAME, data.title)
                }
                intent.putExtra(AppConstants.EXTRA_CAT_ID, data.id)
                startActivity(intent)
                AppUtils.startFromRightToLeft(it)
            }
        }
    }
}