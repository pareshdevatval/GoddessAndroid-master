package com.krystal.goddesslifestyle.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.databinding.FragmentTutorialWaveBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils.getAnimationSize
import com.krystal.goddesslifestyle.utils.WaveView
import com.krystal.goddesslifestyle.viewmodels.TutorialWaveViewModel

/**
 * Created by imobdev on 21/2/20
 */
class TutorialWaveFragment : BaseFragment<TutorialWaveViewModel>() {
    private lateinit var mViewModel: TutorialWaveViewModel
    private lateinit var binding: FragmentTutorialWaveBinding

    private var color: Int = 0
    private var img: Int = 0

    companion object {
        fun newInstance(bundle: Bundle) = TutorialWaveFragment().apply {
            arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentTutorialWaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        color = arguments!!.getInt(AppConstants.KEY_COLOR_CODE)
        img = arguments!!.getInt(AppConstants.KEY_IMG_CODE)
        init()
    }

    override fun getViewModel(): TutorialWaveViewModel {
        mViewModel = ViewModelProvider(this).get(TutorialWaveViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun init() {
        binding.ivTutorialImage.setImageResource(img)
        setWaveAnimation(R.color.color_bg_pink)
    }


    /* Wave Animation */
    @SuppressLint("NewApi")
    private fun setWaveAnimation(color: Int) {
        /* Left to Right */
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (getAnimationSize(context!!) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (1000 + Math.random() * 600).toLong(),
                true
            )
        )
        /* Left to Right */
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (getAnimationSize(context!!) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                true
            )
        )

        /* Right to Left */
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (getAnimationSize(context!!) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                false
            )
        )
        binding.wave.startAnimation()
    }
}