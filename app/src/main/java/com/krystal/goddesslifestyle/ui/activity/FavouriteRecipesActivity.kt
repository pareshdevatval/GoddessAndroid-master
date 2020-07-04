package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.FavouritesRecipesAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.FavouritesResponse
import com.krystal.goddesslifestyle.data.response.LikeUnlikeResponse
import com.krystal.goddesslifestyle.databinding.ActivityFavouriteRecipesBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.of_the_month.OfTheMonthActivity
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.GoddessAnimations
import com.krystal.goddesslifestyle.viewmodels.FavouriteRecipesModel
import okhttp3.internal.notify
import javax.inject.Inject

class FavouriteRecipesActivity : BaseActivity<FavouriteRecipesModel>(),
    BaseBindingAdapter.ItemClickListener<FavouritesResponse.Result?> {


    private lateinit var binding: ActivityFavouriteRecipesBinding
    private lateinit var mViewModel: FavouriteRecipesModel

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase
    private var likedata: FavouritesResponse.Result? = null
    var likeSelectedPositon = 0

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, FavouriteRecipesActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): FavouriteRecipesModel {
        mViewModel = ViewModelProvider(this).get(FavouriteRecipesModel::class.java)
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
        requestsComponent.injectFavouriteRecipesActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favourite_recipes)
        mViewModel.setInjectable(apiService, prefs)
        init()
    }

    fun init() {
        binding.rvFavRec.layoutManager = LinearLayoutManager(this)
        val favouritesRecipesAdapter = FavouritesRecipesAdapter()
        binding.rvFavRec.adapter = favouritesRecipesAdapter
        favouritesRecipesAdapter.filterable = true
        favouritesRecipesAdapter.itemClickListener = this
        setToolBar(getString(R.string.lbl_favourites_recipes), R.color.color_bg_green)
        mViewModel.getFavouritesResponse().observe(this, favouritesRecipesObsrerve)
        mViewModel.getFavouriteResponse().observe(this, favouriteRecipesObsrerve)
        mViewModel.callRecipeListdApi()
        loadMore()

        binding.viewNoNotification.btnViewRecipe.setOnClickListener {
            startActivity(MainActivity.newInstance(this, getString(R.string.title_recipe), false))
            GoddessAnimations.finishFromLeftToRight(this)
        }
    }

    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color

        setToolbarColor(bgColor)
        // bgcolor of dummyView below tab layout

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_search,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    showSearchBar()
                    setSearch()
                }
            })
    }

    /*adepter item click */
    override fun onItemClick(view: View, data: FavouritesResponse.Result?, position: Int) {
        when (view.id) {
            R.id.ivBreakfastFav -> {
                likeSelectedPositon = position
                likedata = data
                if (data?.recipeIsLiked == 1) {
                    mViewModel.callLikeUnlikeRecipeAPI(data!!.recipeId, 2)
                } else {
                    mViewModel.callLikeUnlikeRecipeAPI(data!!.recipeId, 1)
                }
            }
            else -> {
                val subscriptionStatus = AppUtils.getUserSubscription(this)
                if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                    AppUtils.startSubscriptionActivity(this)
                } else {
                    startActivity(
                        RecipeDetailsActivity.newInstance(
                            this, data?.recipeTitle!!,
                            data?.recipeId.toString()
                        )
                    )
                    AppUtils.startFromRightToLeft(this)
                }
            }
        }
    }


    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvFavRec.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvFavRec.layoutManager as LinearLayoutManager,
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
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvFavRec.adapter as FavouritesRecipesAdapter).itemCount
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callRecipeListdApi(CURRENT_PAGE, false)
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
    /*observer recipes data*/
    private val favouritesRecipesObsrerve =
        Observer<FavouritesResponse> { response: FavouritesResponse? ->
            if (response!!.status) {
                if (CURRENT_PAGE == 1) {
                    TOTAL_PAGE = response.pagination.lastPage

                    if (response.result.isNotEmpty()) {
                        binding.rvFavRec.visibility = View.VISIBLE
                        binding.llNoData.visibility = View.GONE
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).setItem(response.result)
                    } else {
                        binding.rvFavRec.visibility = View.GONE
                        binding.llNoData.visibility = View.VISIBLE
                        AppUtils.startNotificationWaveAnimation(
                            this,
                            binding.viewNoNotification.wave,
                            R.color.green
                        )
                    }
                } else {
                    (binding.rvFavRec.adapter as FavouritesRecipesAdapter).removeLoadingFooter(true)
                    val adapterCount =
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).itemCount
                    (binding.rvFavRec.adapter as FavouritesRecipesAdapter).removeItem(
                        adapterCount - 1
                    )


                    val sizeList =
                        (binding.rvFavRec.adapter as FavouritesRecipesAdapter).itemCount
                    (binding.rvFavRec.adapter as FavouritesRecipesAdapter).addItems(response.result)
                    (binding.rvFavRec.adapter as FavouritesRecipesAdapter).notifyItemRangeInserted(
                        sizeList,
                        response.result.size
                    )
                    isLoading=true
                }

            } else {
                AppUtils.showSnackBar(binding.root, response.message)
            }
        }

    /*local search*/
    private fun setSearch() {
        val editText = getEditTextView()
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (binding.rvFavRec.adapter as FavouritesRecipesAdapter).filter(s.toString().toLowerCase())
            }
        })


        /*hide search bar */
        editText.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val DRAWABLE_RIGHT = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        hideSearchBar()
                    }
                }
                return false
            }
        })

    }
    /*observe recipes favourite data*/
    private val favouriteRecipesObsrerve = Observer<LikeUnlikeResponse> {
        if((binding.rvFavRec.adapter as FavouritesRecipesAdapter).items.size == 1){
            CURRENT_PAGE = 1
            (binding.rvFavRec.adapter as FavouritesRecipesAdapter).clear()
            mViewModel.callRecipeListdApi()
        } else if (it.status && it.favourite == 2) {
            (binding.rvFavRec.adapter as FavouritesRecipesAdapter).items.removeAt(likeSelectedPositon)
            (binding.rvFavRec.adapter as FavouritesRecipesAdapter).notifyItemRemoved(likeSelectedPositon)
            (binding.rvFavRec.adapter as FavouritesRecipesAdapter).notifyItemRangeChanged(likeSelectedPositon,
                (binding.rvFavRec.adapter as FavouritesRecipesAdapter).items.size)
        }
    }
}
