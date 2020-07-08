package com.krystal.goddesslifestyle.ui.recipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.krystal.goddesslifestyle.databinding.FragmentSnacksBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.SnacksViewModel
import javax.inject.Inject

/**
 * Created by imobdev on 23/3/20
 */
class SnacksFragment : BaseFragment<SnacksViewModel>(),
    BaseBindingAdapter.ItemClickListener<BreakfastData?> {
    override fun onItemClick(view: View, data: BreakfastData?, position: Int) {
        if (view.id== R.id.ivBreakfastFav){


            context?.let {
                val subscriptionStatus = AppUtils.getUserSubscription(it)
                if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                    AppUtils.startSubscriptionActivity(context)
                } else {
                    likeSelectedPositon=position
                    likedata=data
                    if (data!!.recipe_is_liked==2){
                        mViewModel.callLikeUnlikeRecipeAPI(data!!.recipe_id,1)
                    }else{
                        mViewModel.callLikeUnlikeRecipeAPI(data!!.recipe_id,2)
                    }
                }
            }

        }else {
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

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): SnacksFragment {
            val bundle = Bundle()
            val fragment = SnacksFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1
    private var likeSelectedPositon=0
    private var likedata: BreakfastData?=null
    private lateinit var mViewModel: SnacksViewModel
    private lateinit var binding: FragmentSnacksBinding
    private lateinit var adapter: BreakfastListAdapter
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val requestsComponent: NetworkLocalComponent =
            DaggerNetworkLocalComponent.builder().networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent()).build()
        requestsComponent.injectSnacksFragment(this)
        binding = FragmentSnacksBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        mViewModel.setInjectable(apiService, prefs)
        mViewModel.getDinnerListResponseResponse().observe(viewLifecycleOwner,observerDinnerList)
        mViewModel.getFavouriteResponse().observe(viewLifecycleOwner, favouriteObserver)
        mViewModel.callGetRecipeListAPI()
        setDinnerAdapter()
        loadMore()
    }

    private fun setDinnerAdapter() {
        binding.rvDinner.layoutManager = LinearLayoutManager(context!!)
        adapter=BreakfastListAdapter()
        binding.rvDinner.adapter = adapter
        adapter.itemClickListener = this
    }

    private val favouriteObserver = Observer<LikeUnlikeResponse> { response: LikeUnlikeResponse? ->
        if (response!!.status) {
            val data=likedata
            data!!.recipe_is_liked=response.favourite
            adapter.notifyItemChanged(likeSelectedPositon,data)
        }else{
            AppUtils.showSnackBar(binding.root,response.message)
        }
    }


    private val observerDinnerList= Observer<RecipeListReponse>{ response: RecipeListReponse? ->
        if (response!!.status) {
            if(CURRENT_PAGE==1){
                TOTAL_PAGE = response.pagination.lastPage

                if (response.result.isNotEmpty()) {
                    binding.rvDinner.visibility=View.VISIBLE
                    binding.llView.visibility = View.GONE
                    adapter.setItem(response.result)
                }else{
                    //AppUtils.showSnackBar(binding.root, response.message)
                    binding.rvDinner.visibility=View.GONE
                    binding.root.setBackgroundColor(ContextCompat.getColor(context!!, R.color.green))
                    binding.llView.visibility = View.VISIBLE
                    // AppUtils.startWaveAnimation(context!!, binding.viewNoRecipeFound.wave, R.color.green)
                }
            }else{
                (binding.rvDinner.adapter as BreakfastListAdapter).removeLoadingFooter(true)
                val adapterCount =
                    (binding.rvDinner.adapter as BreakfastListAdapter).itemCount

                if(adapterCount != 0) {
                    (binding.rvDinner.adapter as BreakfastListAdapter).removeItem(
                        adapterCount - 1
                    )
                }

                val sizeList =
                    (binding.rvDinner.adapter as BreakfastListAdapter).itemCount
                (binding.rvDinner.adapter as BreakfastListAdapter).addItems(response.result)
                (binding.rvDinner.adapter as BreakfastListAdapter).notifyItemRangeInserted(
                    sizeList,
                    response.result.size
                )
            }

        } else {
            AppUtils.showSnackBar(binding.root, response.message)
        }
    }
    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvDinner.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvDinner.layoutManager as LinearLayoutManager,
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
                        (binding.rvDinner.adapter as BreakfastListAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvDinner.adapter as BreakfastListAdapter).itemCount
                        (binding.rvDinner.adapter as BreakfastListAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvDinner.adapter as BreakfastListAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callGetRecipeListAPI(CURRENT_PAGE,"",false)
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
    override fun getViewModel(): SnacksViewModel {
        mViewModel = ViewModelProvider(this).get(SnacksViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    fun setSearchAction(searchvalue: String) {
        Log.e("SERACH NAME", searchvalue)
        CURRENT_PAGE = 1
        mViewModel.callGetRecipeListAPI(CURRENT_PAGE, searchvalue)
    }

}