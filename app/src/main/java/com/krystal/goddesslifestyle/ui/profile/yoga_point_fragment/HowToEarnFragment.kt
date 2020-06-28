package com.krystal.goddesslifestyle.ui.profile.yoga_point_fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.HowToEarnAdapter
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.model.HowToEarnModel
import com.krystal.goddesslifestyle.databinding.FragmentHowToEarnBinding
import com.krystal.goddesslifestyle.viewmodels.HowToEarnViewModel


/**
 * Created by imobdev on 21/2/20
 */
class HowToEarnFragment : BaseFragment<HowToEarnViewModel>(),
    BaseBindingAdapter.ItemClickListener<HowToEarnModel?> {


    private lateinit var mViewModel: HowToEarnViewModel
    private lateinit var binding: FragmentHowToEarnBinding


    companion object {
        fun newInstance() = HowToEarnFragment().apply {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHowToEarnBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun init() {
        binding.rvHowToEarn.layoutManager = LinearLayoutManager(context)
        val howToEarnAdapter = HowToEarnAdapter()
        binding.rvHowToEarn.adapter = howToEarnAdapter
        howToEarnAdapter.itemClickListener = this
        setData()
        mViewModel.getHowToEarnResponse().observe(this, howToEarnObserve)

    }

    private fun setData() {
        val list: ArrayList<HowToEarnModel?> = ArrayList()
        list.add(
            HowToEarnModel(
                R.drawable.ic_invite_friends,
                "Invite your friends to join the app!",
                1000
            ,1)
        )
        list.add(HowToEarnModel(R.drawable.ic_daily_practice, "Complete your daily practice", 15,2))
        list.add(HowToEarnModel(R.drawable.ic_share_green, "Share your practice", 15,3))
        list.add(HowToEarnModel(R.drawable.ic_journal, "Share journal prompt of the day", 10,4))
        list.add(HowToEarnModel(R.drawable.ic_food_photo, "Share your food photo", 10,5))
        list.add(
            HowToEarnModel(
                R.drawable.ic_shared_month_calender_pink,
                "Share this month's calendar",
                10
            ,6)
        )


        (binding.rvHowToEarn.adapter as HowToEarnAdapter).setItem(list)
        (binding.rvHowToEarn.adapter as HowToEarnAdapter).notifyDataSetChanged()
    }

    override fun getViewModel(): HowToEarnViewModel {
        mViewModel = ViewModelProvider(this).get(HowToEarnViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onItemClick(view: View, data: HowToEarnModel?, position: Int) {

    }

    private val howToEarnObserve = Observer<BaseResponse> {

    }
}