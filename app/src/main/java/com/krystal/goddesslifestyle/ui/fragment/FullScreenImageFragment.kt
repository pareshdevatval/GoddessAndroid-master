package com.krystal.goddesslifestyle.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krystal.goddesslifestyle.BuildConfig
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.databinding.FragmentFullScreenImageBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

class FullScreenImageFragment : BaseFragment<BaseViewModel>() {

    private lateinit var binding: FragmentFullScreenImageBinding

    companion object {
        fun newInstance(bundle: Bundle) = FullScreenImageFragment().apply {
            arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFullScreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUrl = arguments!!.getString(AppConstants.Images_URL, "")
        binding.imageUrl = BuildConfig.IMAGE_BASE_URL + imageUrl
    }

    override fun getViewModel() = BaseViewModel(GoddessLifeStyleApp())

    override fun internetErrorRetryClicked() {

    }
}