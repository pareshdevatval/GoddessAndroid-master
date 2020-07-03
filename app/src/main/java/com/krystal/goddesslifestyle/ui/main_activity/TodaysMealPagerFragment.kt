package com.krystal.goddesslifestyle.ui.main_activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewpagerImagesAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.Recipe
import com.krystal.goddesslifestyle.databinding.PagerFragmentTodayMealBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.TodaysMealViewModel
import javax.inject.Inject

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class TodaysMealPagerFragment : BaseFragment<TodaysMealViewModel>(), View.OnClickListener {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): TodaysMealPagerFragment {
            val bundle = Bundle()
            val fragment = TodaysMealPagerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: TodaysMealViewModel
    private lateinit var binding: PagerFragmentTodayMealBinding

    private val recipesList: ArrayList<Recipe> = ArrayList()
    private lateinit var todayRecipeIds: List<Int>

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    override fun getViewModel(): TodaysMealViewModel {
        viewModel = ViewModelProvider(this).get(TodaysMealViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectTodaysMealPagerFragment(this)

        binding = PagerFragmentTodayMealBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.mainLayout.clipToOutline = true
        //setUpViewPager()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInjectable(apiService, prefs)
        viewModel.getShareApiResponse().observe(viewLifecycleOwner, shareApiResponseObserver)
    }

    private fun setUpViewPager() {
        val imagesList: ArrayList<String> = ArrayList()
        for (recipe in recipesList) {
            imagesList.add(
                if (appDatabase.recipeImageDao()
                        .getRecipeImages(recipe.recipeId).isNotEmpty()
                ) appDatabase.recipeImageDao().getRecipeImages(recipe.recipeId)[0]
                else ""
            )
        }
        val adapter = ViewpagerImagesAdapter(context!!, imagesList)
        binding.viewPager.adapter = adapter
        binding.vpIndicator.setViewPager(binding.viewPager)

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (recipesList.isNotEmpty()) {
                    setUpRecipeData(recipesList[position], position)
                }
            }
        })
    }

    /*Accepts
    @calenderId : day id for which we have to show Data
    If user selected 9th of the month then in "calenderDayId", the id of 9th date will be passed
    This would be needed to retrieve the data of this date from db*/
    fun setCalenderDay(calenderDayId: Int) {
        recipesList.clear()
        // we can get today's recipe id from currently selected calenderDay
        todayRecipeIds = appDatabase.todaysRecipeDao().getTodayRecipeId(calenderDayId)
        /*And from today's recipe id, We can get the object of Recipe*/
        for (id in todayRecipeIds) {
            val recipe = appDatabase.recipeDao().getRecipe(id)
            recipe?.let {
                recipesList.add(recipe)
                setUpViewPager()
            }
        }
        if (recipesList.isNotEmpty()) {
            setUpRecipeData(recipesList[0], 0)
        }
    }

    fun setUpRecipeData(recipe: Recipe, position: Int) {
        binding.tvCalories.text = recipe.recipeCalories
        binding.tvTime.text = recipe.recipeDuration
        binding.tvMealName.text = recipe.recipeTitle

        var mealType = ""
        when (position) {
            0 -> mealType = getString(R.string.breakfast)
            1 -> mealType = getString(R.string.lunch)
            2 -> mealType = getString(R.string.dinner)
        }
        binding.tvTodaysMealPlan.text =
            String.format(getString(R.string.meal_type), mealType, recipe.recipeTitle)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.shareLayout -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(context, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            val vpIndex = binding.viewPager.currentItem
                            val recipe = recipesList[vpIndex]

                            val imageUrl =
                                appDatabase.recipeImageDao().getRecipeImages(recipe.recipeId)[0]

                            if (imageUrl.isNotEmpty()) {
                                AppUtils.shareContent(it, AppUtils.generateImageUrl(imageUrl))
                            } else {
                                AppUtils.shareContent(
                                    it,
                                    "Share from Goddess LifeStyle Android App"
                                )
                            }
                            callShareApi()
                        }
                    }
                }
            }
            R.id.btn_view_recipe -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        //AppUtils.showToast(it, "Under Development")
                        val recipe = recipesList[binding.viewPager.currentItem]
                        startActivity(
                            RecipeDetailsActivity.newInstance(
                                context!!, recipe.recipeTitle,
                                recipe.recipeId.toString()
                            )
                        )
                        AppUtils.startFromRightToLeft(context!!)
                    }
                }
            }
        }
    }

    private fun callShareApi() {
        val params = HashMap<String, String>()
        params[ApiContants.ACTIVITY_ID] =
            todayRecipeIds.get(binding.viewPager.currentItem).toString()
        params[ApiContants.EARNED_POINTS] = ApiContants.SHARE_RECIPE_POINTS.toString()
        params[ApiContants.TYPE] = ApiContants.SHARE_TYPE_RECIPE
        viewModel.callShareApi(params)
    }

    private val shareApiResponseObserver = Observer<BaseResponse> {
        if (it.status) {
            //AppUtils.showToast(context!!, it.message)
        }
    }
}