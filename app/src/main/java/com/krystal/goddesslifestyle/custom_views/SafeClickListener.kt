package com.krystal.goddesslifestyle.custom_views

import android.os.SystemClock
import android.view.View

/**
 * Created by Bhargav Thanki on 15 April,2020.
 */
abstract class SafeClickListener(
    private var defaultInterval: Int = 1000
    ) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    abstract fun onSafeCLick(v: View) : Unit

    override fun onClick(v: View) {
    if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
        return
    }
    lastTimeClicked = SystemClock.elapsedRealtime()
    onSafeCLick(v)
    }
}