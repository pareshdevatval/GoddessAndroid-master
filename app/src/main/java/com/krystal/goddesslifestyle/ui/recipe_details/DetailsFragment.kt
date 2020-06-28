package com.krystal.goddesslifestyle.ui.recipe_details

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.databinding.FragmentRecipeDetailsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.viewmodels.DetailsViewModel
import kotlinx.android.synthetic.main.activity_of_the_month.*

class DetailsFragment : BaseFragment<DetailsViewModel>() {
    private lateinit var mViewModel: DetailsViewModel
    private lateinit var binding: FragmentRecipeDetailsBinding

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): DetailsFragment {
            val bundle = Bundle()
            val fragment = DetailsFragment()
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
        requestsComponent.injectDetailsFragment(this)
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        animateTopView()
    }

    private fun init() {
        setRecipeData()
    }

    private fun setRecipeData() {
        (activity as RecipeDetailsActivity).recipeDetailsResponse.let { it ->
            it.let {
               binding.recipeDetails=it
                if (it!!.result.recipe_images.isNotEmpty()){
                    Log.e("IMG",""+it!!.result.recipe_images[0])
                    binding.tvMinute.text=it.result.recipe_duration
                    binding.image=it.result.recipe_images[0]
                }
            }
        }
    }

    override fun getViewModel(): DetailsViewModel {
        mViewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {
    }

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
                    (activity as RecipeDetailsActivity).dummyView.visibility = View.GONE
                }
                /*if(p1 == 0) {
                    // Top scroll
                    (activity as OfTheMonthActivity).dummyView.visibility = View.VISIBLE
                }*/

                if (!binding.scrollView.canScrollVertically(-1)) {
                    (activity as RecipeDetailsActivity).dummyView.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && ::binding.isInitialized) {
            if (!binding.scrollView.canScrollVertically(-1)) {
                (activity as RecipeDetailsActivity).dummyView.visibility = View.VISIBLE
            } else {
                (activity as RecipeDetailsActivity).dummyView.visibility = View.GONE
            }
        }
    }

}
