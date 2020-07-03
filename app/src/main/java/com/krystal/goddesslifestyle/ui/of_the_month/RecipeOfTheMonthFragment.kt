package com.krystal.goddesslifestyle.ui.of_the_month

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.response.Recipe
import com.krystal.goddesslifestyle.data.response.RecipeOfTheMonth
import com.krystal.goddesslifestyle.databinding.FragmentRecipeOfTheMonthBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.community.PlayVideoActivity
import com.krystal.goddesslifestyle.ui.recipe_details.RecipeDetailsActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import kotlinx.android.synthetic.main.activity_of_the_month.*

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class RecipeOfTheMonthFragment : BaseFragment<BaseViewModel>(), View.OnClickListener {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment
        */
        fun newInstance(): RecipeOfTheMonthFragment {
            val bundle = Bundle()
            val fragment = RecipeOfTheMonthFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: BaseViewModel
    private lateinit var binding: FragmentRecipeOfTheMonthBinding

    private var currentRecipe: RecipeOfTheMonth? = null

    override fun getViewModel(): BaseViewModel {
        viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectRecipeOfTheMonthFragment(this)

        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRecipeOfTheMonthBinding.inflate(inflater, container, false)
        binding.fragment = this

        setUpData()

        animateTopView()

        return binding.root
    }

    private fun setUpData() {
        (activity as OfTheMonthActivity).ofTheMonthData?.let {
            val recipeOfTheMonth = it.recipes
            recipeOfTheMonth?.let { recipe ->
                currentRecipe = recipe
                binding.recipe = recipe
                binding.ivRecipe.post {
                    context?.let {
                        val recipeImages = recipe.recipeImages
                        recipeImages?.let { recipeImage ->
                            val firstImageObject = recipeImage.get(0)
                            firstImageObject?.let { imageObj ->
                                val image = imageObj.image
                                image?.let { img ->
                                    AppUtils.loadImageThroughGlide(
                                        it, binding.ivRecipe,
                                        AppUtils.generateImageUrl(
                                            img,
                                            binding.ivRecipe.width,
                                            binding.ivRecipe.height
                                        ),
                                        R.drawable.ic_placeholder_square
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*Code snippet taken from SO to detext the scroll change direction and show/hide top view based on that*/
    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateTopView() {
        binding.scrollView.setOnScrollChangeListener(object : View.OnScrollChangeListener {
            override fun onScrollChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int) {
                if (p1 > p4) {
                    // scroll down
                    //(activity as OfTheMonthActivity).dummyView.visibility = View.GONE
                }
                if (p1 < p4) {
                    // scroll up
                    (activity as OfTheMonthActivity).dummyView.visibility = View.GONE
                }
                /*if(p1 == 0) {
                    // Top scroll
                    (activity as OfTheMonthActivity).dummyView.visibility = View.VISIBLE
                }*/

                if (!binding.scrollView.canScrollVertically(-1)) {
                    (activity as OfTheMonthActivity).dummyView.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        /*to keep the state of scroll and hence show/hide dummy view*/
        if (isVisibleToUser && ::binding.isInitialized) {
            if (!binding.scrollView.canScrollVertically(-1)) {
                (activity as OfTheMonthActivity).dummyView.visibility = View.VISIBLE
            } else {
                (activity as OfTheMonthActivity).dummyView.visibility = View.GONE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnViewRecipe -> {
                //AppUtils.startSubscriptionActivity(context)
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (currentRecipe != null && currentRecipe!!.recipeTitle != null) {
                            startActivity(
                                RecipeDetailsActivity.newInstance(
                                    context!!, currentRecipe?.recipeTitle!!,
                                    currentRecipe?.recipeId.toString()
                                )
                            )
                            AppUtils.startFromRightToLeft(context!!)
                        }
                    }
                }
            }
        }
    }
}