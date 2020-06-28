package com.krystal.goddesslifestyle.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Created by Bhargav Thanki on 24 February,2020.
 */
class ViewPagerAdapterForTabs(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitlesList = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position];
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title:String) {
        mFragmentList.add(fragment)
        mFragmentTitlesList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitlesList[position]
    }
}