package com.krystal.goddesslifestyle.ui.recipe_details

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.adapter.RecipeStepAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.databinding.FragmentHowToCookBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.viewmodels.HowToCookViewModel
import kotlinx.android.synthetic.main.activity_of_the_month.*

class HowToCookFragment : BaseFragment<HowToCookViewModel>(){

    private lateinit var mViewModel: HowToCookViewModel
    private lateinit var binding: FragmentHowToCookBinding
    private lateinit var adapter: RecipeStepAdapter
    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): HowToCookFragment {
            val bundle = Bundle()
            val fragment = HowToCookFragment()
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
        requestsComponent.injectHowToCookFragment(this)
        binding = FragmentHowToCookBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        animateTopView()
    }

    private fun init() {
        setAdapter()
        setRecipeData()
    }

    private fun setRecipeData() {
        (activity as RecipeDetailsActivity).recipeDetailsResponse.let { it ->
            it.let {
                if (it!!.result.recipe_images.isNotEmpty()){
                    Log.e("IMG",""+it!!.result.recipe_images[0])
                    binding.tvMinute.text=it.result.recipe_duration
                    binding.image=it.result.recipe_images[0]
                    if (it.result.recipe_steps.isNotEmpty()) {
                        adapter.setItem(it.result.recipe_steps)
                    }
                }
            }
        }
    }

    override fun getViewModel(): HowToCookViewModel {
        mViewModel = ViewModelProvider(this).get(HowToCookViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun setAdapter() {
        binding.rvSteps.layoutManager = LinearLayoutManager(context!!)
        adapter = RecipeStepAdapter()
        binding.rvSteps.adapter = adapter
    }
}
