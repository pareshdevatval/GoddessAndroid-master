package com.krystal.goddesslifestyle.ui.video_library

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.FilterAdapter
import com.krystal.goddesslifestyle.adapter.VideosAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.Video
import com.krystal.goddesslifestyle.data.response.VideoCategoriesResponse
import com.krystal.goddesslifestyle.data.response.VideosListResponse
import com.krystal.goddesslifestyle.databinding.ActivityVideoDetailsListBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.community.PlayVideoActivity
import com.krystal.goddesslifestyle.utils.*
import com.krystal.goddesslifestyle.viewmodels.VideoDetailsViewModel
import javax.inject.Inject

class VideoDetailsListActivity : BaseActivity<VideoDetailsViewModel>(), BaseBindingAdapter.ItemClickListener<Video>,
View.OnClickListener{


    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, VideoDetailsListActivity::class.java)
            return intent
        }
    }

    private lateinit var viewModel: VideoDetailsViewModel
    private lateinit var binding: ActivityVideoDetailsListBinding

    var subCategories : ArrayList<VideoCategoriesResponse.VideoCategory.VideoSubCategory>? = null
    /*selected category name*/
    var categoryName: String? = null
    /*selected category id*/
    var categoryId: Int? = null
    var subCategoryId: Int? = null

    /*filter window for sub-categories*/
    var filterPopupWindow: ListPopupWindow? = null
    /*selected filter of sub category to show selected in window*/
    var selectedFilterPosition = 0

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    override fun getViewModel(): VideoDetailsViewModel {
        viewModel = ViewModelProvider(this).get(VideoDetailsViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectVideoDetailsListActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_details_list)
        binding.activity = this
        super.onCreate(savedInstanceState)

        viewModel.setInjectable(apiService, prefs)

        viewModel.getVideosResponse().observe(this, videosResponseObserver)


        initializeFilter()
    }

    private fun callApi(fromFilter: Boolean = false) {
        val params = HashMap<String, String>()
        if(fromFilter && subCategoryId != -1) {
            /*If this method is called by selecting any subcategory from list popup window*,
            then we will send CHILD_ID param in request/
             */
            params[ApiContants.CHILD_ID] = subCategoryId.toString()
        } else {
            /*If an API is being callend for first time and we have to get the data from all subscategories
            * then we will send PARENT_ID param in request*/
            params[ApiContants.PARENT_ID] = categoryId.toString()
        }
        viewModel.callVideosApi(params)
    }

    /*initializing the filter of the subcategories*/
    private fun initializeFilter() {
        val subCategoriesString: ArrayList<String> = ArrayList()
        subCategories =
            intent?.getParcelableArrayListExtra<VideoCategoriesResponse.VideoCategory.VideoSubCategory>(AppConstants.EXTRA_SUB_CATEGORIES)
        subCategories?.let {
            for (i in 0 until it.size) {
                subCategoriesString.add(it[i].title!!)
            }
        }
        categoryName = intent?.getStringExtra(AppConstants.EXTRA_CAT_NAME)

        categoryName?.let {
            val allFilter = String.format(getString(R.string.all_filter), it)
            subCategoriesString.add(0, allFilter)
        }

        categoryId = intent?.getIntExtra(AppConstants.EXTRA_CAT_ID, -1)

        init(subCategoriesString)

        initFilter(subCategoriesString)

        callApi()
    }

    private fun init(filterList: ArrayList<String>) {
        setToolbarTitle(getString(R.string.title_video_library))

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : BaseActivity.ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        if(filterList.isNotEmpty()) {
            // toolbar right icon and its click listener
            setToolbarRightIcon(R.drawable.ic_filter, object : BaseActivity.ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    //AppUtils.showSnackBar(binding.dummyView, "Share")
                    //initFilter(filterList)
                    filterPopupWindow?.show()
                }
            })
        }

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = linearLayoutManager

        val adapter = VideosAdapter()
        binding.recyclerView.adapter = adapter
        adapter.itemClickListener = this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoddessAnimations.finishFromLeftToRight(this)
    }

    private fun initFilter(filterList: ArrayList<String>) {
        filterPopupWindow = ListPopupWindow(this)
        //val adapter = ArrayAdapter(this, R.layout.filter_drop_down, filterList)
        val adapter = FilterAdapter(this, filterList)
        filterPopupWindow?.setAdapter(adapter)

        filterPopupWindow?.anchorView = getToolbarRightIcon()
        filterPopupWindow?.isModal = true
        filterPopupWindow?.setContentWidth(AppUtilsJava.measureContentWidth(adapter, this))
        filterPopupWindow?.setDropDownGravity(Gravity.RIGHT)
        filterPopupWindow?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.filter_bg))

        filterPopupWindow?.setOnItemClickListener(object : AdapterView.OnItemClickListener{
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedFilterPosition = p2
                // to show a selection arrow on the selected item
                adapter.setSelectedPosition(p2)
                filterPopupWindow?.dismiss()
                subCategoryId = if(p2 == 0) {
                    -1
                } else {
                    subCategories?.get(p2-1)?.id
                }
                callApi(true)
            }
        })

        //filterPopupWindow?.show()

    }

    private val videosResponseObserver = Observer<VideosListResponse>() {
        if(it.status) {
            if(it.videos != null) {
                val videos= it.videos
                if(videos!!.isNotEmpty()) {
                    showToolbar()
                    binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.pink))
                    (binding.recyclerView.adapter as VideosAdapter).setItem(videos as ArrayList<Video>)
                    binding.noDataView.visibility = View.GONE
                } else {
                    // No videos found
                    // showinf view for that
                    hideToolbar()
                    binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    binding.noDataView.visibility = View.VISIBLE
                    AppUtils.startWaveAnimation(this, binding.wave, R.color.pink)
                }
            } else {
                // No videos found
                // showinf view for that
                hideToolbar()
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.noDataView.visibility = View.VISIBLE
                AppUtils.startWaveAnimation(this, binding.wave, R.color.pink)
            }
        }
    }

    override fun onItemClick(view: View, data: Video, position: Int) {
        //AppUtils.showToast(this, "Playing test video")
        startActivity(PlayVideoActivity.newInstance(this,AppUtils.getVideoUrl(data.vlVideo)))
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        when(v?.id) {
            R.id.btn_go_back -> {
                onBackPressed()
            }
        }
    }
}
