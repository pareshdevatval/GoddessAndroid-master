package com.krystal.goddesslifestyle.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.krystal.goddesslifestyle.R

/**
 * Created by Bhargav Thanki on 25 February,2020.
 */
object GoddessAnimations {

    fun startFromRightToLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.trans_left_in,
            R.anim.trans_left_out
        )
    }

    fun finishFromLeftToRight(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.trans_right_in,
            R.anim.trans_right_out
        )
    }

    fun startFromBottomToUp(activity: Activity) {
        activity.overridePendingTransition(R.anim.trans_bottom_up, R.anim.no_animation)
    }

    fun finishFromUpToBottom(activity: Activity) {
        activity.overridePendingTransition(R.anim.no_animation, R.anim.trans_up_bottom)
    }

    fun finishFromRightToLeft(activity: Activity) {
        activity.overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out)
    }

    fun animateViewFromRightToLeft(context: Context?, view: View, listener: GoddessAnimationListener) {
        context?.let {
            val animation = AnimationUtils.loadAnimation(it, R.anim.trans_left_in_with_bounce)
            view.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    listener.onAnimationEnd()
                }

                override fun onAnimationStart(p0: Animation?) {

                }
            })
        }
    }



    fun animateViewFromLeftToRight(context: Context?, view: View, listener: GoddessAnimationListener) {
        context?.let {
            val animation = AnimationUtils.loadAnimation(it, R.anim.trans_right_out)
            view.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    /*binding.viewPager.visibility = View.GONE
                    binding.shareLayout.visibility = View.VISIBLE*/
                    listener.onAnimationEnd()
                }

                override fun onAnimationStart(p0: Animation?) {

                }
            })
        }
    }

    interface GoddessAnimationListener {
        fun onAnimationStart()
        fun onAnimationEnd()
    }
}