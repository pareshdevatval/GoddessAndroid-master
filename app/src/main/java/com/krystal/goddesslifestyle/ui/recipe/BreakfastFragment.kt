package com.krystal.goddesslifestyle.ui.recipe

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.BreakfastListAdapter
import com.krystal.goddesslifestyle.base.BaseBindingAdapter

import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.BreakfastData
import com.krystal.goddesslifestyle.data.response.LikeUnlikeResponse
import com.krystal.goddesslifestyle.data.response.RecipeListReponse
import com.krystal.goddesslifestyle.databinding.FragmentBreakfastBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.BreakfastViewModel
import javax.inject.Inject

/**
 * Created by imobdev on 23/3/20
 */
class BreakfastFragment : BaseFragment<BreakfastViewModel>(),
    BaseBindingAdapter.ItemClickListener<BreakfastData?> {
    override fun onItemClick(view: View, data: BreakfastData?, position: Int) {
        if (view.id == R.id.ivBreakfastFav) {

            context?.let {
                val subscriptionStatus = AppUtils.getUserSubscription(it)
                if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                    AppUtils.startSubscriptionActivity(context)
                } else {
                    likeSelectedPositon = position
                    likedata = data
                    if (data!!.recipe_is_liked == 2) {
                        mViewModel.callLikeUnlikeRecipeAPI(data!!.recipe_id, 1)
                    } else {
                        mViewModel.callLikeUnlikeRecipeAPI(data!!.recipe_id, 2)
                    }
                }
            }

        } else {
            context?.let {
                val subscriptionStatus = AppUtils.getUserSubscription(it)
                if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                    AppUtils.startSubscriptionActivity(context)
                } else {
                    startActivity(RecipeDetailsActivity.newInstance(context!!,data!!.recipe_title,
                            data.recipe_id.toString()))
                    AppUtils.startFromRightToLeft(context!!)
                }
            }

        }
    }

    private lateinit var mViewModel: BreakfastViewModel
    private lateinit var binding: FragmentBreakfastBinding
    private lateinit var breakfastAdapter: BreakfastListAdapter

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1

    private var likeSelectedPositon = 0
    private var likedata: BreakfastData? = null
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): BreakfastFragment {
            val bundle = Bundle()
            val fragment = BreakfastFragment()
            fragment.arguments = bundle
            return fragment
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
        requestsComponent.injectBreakfastFragment(this)
        binding = FragmentBreakfastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun init() {
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.getBreakFastResponseResponse().observe(this, observerBreakFastDat)
        mViewModel.getFavouriteResponse().observe(this, favouriteObserver)
        mViewModel.callGetRecipeListAPI()
        setBreakfastAdapter()
        loadMore()
    }

    private val favouriteObserver = Observer<LikeUnlikeResponse> { response: LikeUnlikeResponse? ->
        if (response!!.status) {
            val data = likedata
            data!!.recipe_is_liked = response.favourite
            breakfastAdapter.notifyItemChanged(likeSelectedPositon, data)
        } else {
            AppUtils.showSnackBar(binding.root, response.message)
        }
    }
    private val observerBreakFastDat = Observer<RecipeListReponse> { response: RecipeListReponse? ->
        if (response!!.status) {

            if (CURRENT_PAGE == 1) {
                TOTAL_PAGE = response.pagination.total

                if (response.result.isNotEmpty()) {
                  //  showToolbar()
                    binding.rvBreakfast.visibility=View.VISIBLE
                    binding.llView.visibility=View.GONE
                    breakfastAdapter.setItem(response.result)
                } else {
                  //  AppUtils.showSnackBar(binding.root, response.message)
                  //  hideToolbar()
                    binding.rvBreakfast.visibility=View.GONE
                    binding.llView.visibility = View.VISIBLE
                    //AppUtils.startWaveAnimation(context!!, binding.viewNoRecipeFound.wave, R.color.green)
                }
            } else {
                (binding.rvBreakfast.adapter as BreakfastListAdapter).removeLoadingFooter(true)
                val adapterCount =
                    (binding.rvBreakfast.adapter as BreakfastListAdapter).itemCount
                (binding.rvBreakfast.adapter as BreakfastListAdapter).removeItem(
                    adapterCount - 1
                )


                val sizeList =
                    (binding.rvBreakfast.adapter as BreakfastListAdapter).itemCount
                (binding.rvBreakfast.adapter as BreakfastListAdapter).addItems(response.result)
                (binding.rvBreakfast.adapter as BreakfastListAdapter).notifyItemRangeInserted(
                    sizeList,
                    response.result.size
                )
            }

        } else {
            AppUtils.showSnackBar(binding.root, response.message)
        }
    }

    private fun setBreakfastAdapter() {
        binding.rvBreakfast.layoutManager = LinearLayoutManager(context!!)
        breakfastAdapter = BreakfastListAdapter()
        binding.rvBreakfast.adapter = breakfastAdapter
        breakfastAdapter.itemClickListener = this
    }


    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvBreakfast.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvBreakfast.layoutManager as LinearLayoutManager,
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
                        (binding.rvBreakfast.adapter as BreakfastListAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvBreakfast.adapter as BreakfastListAdapter).itemCount
                        (binding.rvBreakfast.adapter as BreakfastListAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvBreakfast.adapter as BreakfastListAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callGetRecipeListAPI(CURRENT_PAGE, "", false)
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

    override fun internetErrorRetryClicked() {

    }

    override fun getViewModel(): BreakfastViewModel {
        mViewModel = ViewModelProvider(this).get(BreakfastViewModel::class.java)
        return mViewModel
    }

    fun setSearchAction(searchvalue: String) {
        Log.e("SERACH NAME", searchvalue)
        CURRENT_PAGE = 1
        mViewModel.callGetRecipeListAPI(CURRENT_PAGE, searchvalue)
    }
}