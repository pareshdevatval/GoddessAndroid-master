package com.krystal.goddesslifestyle.ui.profile.yoga_point_fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.adapter.YourPointAdapter
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.YourPointsResponse
import com.krystal.goddesslifestyle.databinding.FragmentYourPointsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.YourPointViewModel
import javax.inject.Inject


/**
 * Created by imobdev on 21/2/20
 */
class YourPointFragment : BaseFragment<YourPointViewModel>(),
    BaseBindingAdapter.ItemClickListener<YourPointsResponse.Result?> {
    override fun onItemClick(view: View, data: YourPointsResponse.Result?, position: Int) {

    }

    private lateinit var mViewModel: YourPointViewModel
    private lateinit var binding: FragmentYourPointsBinding

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1
    companion object {
        fun newInstance() = YourPointFragment().apply {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val requestsComponent: NetworkLocalComponent =
            DaggerNetworkLocalComponent.builder().networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent()).build()
        requestsComponent.injectYourPointFragment(this)
        binding = FragmentYourPointsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setInjectable(apiService, prefs)
        init()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun init() {
        mViewModel.getYourPointsResponse().observe(this, yourPointObserve)
        binding.rvYourPoint.layoutManager = LinearLayoutManager(context)
        val howToEarnAdapter = YourPointAdapter()
        binding.rvYourPoint.adapter = howToEarnAdapter
        howToEarnAdapter.itemClickListener = this
        //setData()
        mViewModel.callYouPointApi(1)
        loadMore()
    }
    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvYourPoint.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvYourPoint.layoutManager as LinearLayoutManager,
                true
            ) {
            override fun loadMoreItems() {

                /*If we have load all total pages then no need to go for next*/
                if (TOTAL_PAGE != CURRENT_PAGE) {
                    /*checking for loading flag to prevent too many calls at same time*/
                    if (isLoading) {
                        /*switching the value of loading flag*/
                        isLoading = false
                        /*increment the current page count*/
                        CURRENT_PAGE += 1

                        /*Adding a dummy(null) item to the list to show a footer progress bar*/
                        (binding.rvYourPoint.adapter as YourPointAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvYourPoint.adapter as YourPointAdapter).itemCount
                        (binding.rvYourPoint.adapter as YourPointAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvYourPoint.adapter as YourPointAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callYouPointApi(CURRENT_PAGE,false)
                    }
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGE
            }

            override fun isLastPage(): Boolean {
                return false
            }

            override fun isLoading(): Boolean {
                return false
            }

        })

    }

    private val yourPointObserve = Observer<YourPointsResponse> { response: YourPointsResponse? ->
        if (response!!.status) {
            if(CURRENT_PAGE==1){
                TOTAL_PAGE = response.pagination.lastPage

                if (response.result.isNotEmpty()) {
                    binding.rvYourPoint.visibility=View.VISIBLE
                    (binding.rvYourPoint.adapter as YourPointAdapter).setItem(response.result)
                }else{
                    binding.rvYourPoint.visibility=View.GONE
                }
            }else{
                (binding.rvYourPoint.adapter as YourPointAdapter).removeLoadingFooter(true)
                val adapterCount =
                    (binding.rvYourPoint.adapter as YourPointAdapter).itemCount
                (binding.rvYourPoint.adapter as YourPointAdapter).removeItem(
                    adapterCount - 1
                )


                val sizeList =
                    (binding.rvYourPoint.adapter as YourPointAdapter).itemCount
                (binding.rvYourPoint.adapter as YourPointAdapter).addItems(response.result)
                (binding.rvYourPoint.adapter as YourPointAdapter).notifyItemRangeInserted(
                    sizeList,
                    response.result.size
                )
                isLoading=true
            }

        } else {
            AppUtils.showSnackBar(binding.root, response.message)
        }
    }

    override fun getViewModel(): YourPointViewModel {
        mViewModel = ViewModelProvider(this).get(YourPointViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }


}