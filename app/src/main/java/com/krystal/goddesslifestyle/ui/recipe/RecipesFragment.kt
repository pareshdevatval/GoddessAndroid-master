package com.krystal.goddesslifestyle.ui.recipe

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.ViewPagerAdapterForTabs
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.databinding.FragmentRecipesBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.WebViewActivity
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.recipe.BreakfastFragment
import com.krystal.goddesslifestyle.ui.recipe.DinnerFragment
import com.krystal.goddesslifestyle.ui.recipe.HowToAddRecipeActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.RecipeViewModel
import com.yalantis.colormatchtabs.colormatchtabs.adapter.ColorTabAdapter
import com.yalantis.colormatchtabs.colormatchtabs.listeners.ColorTabLayoutOnPageChangeListener


/**
 * Created by Bhargav Thanki on 16 March,2020.
 */
class RecipesFragment : BaseFragment<RecipeViewModel>() {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): RecipesFragment {
            val bundle = Bundle()
            val fragment = RecipesFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: RecipeViewModel
    private lateinit var binding: FragmentRecipesBinding
    private lateinit var adapter: ViewPagerAdapterForTabs

    /*variable for tab to select when an activity starts*/
    var tabIndexToSelect: Int = 0

    var searchvalue: String = ""
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
        requestsComponent.injectRecipesFragment(this)

        binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        binding.recipeFragment=this
        (activity as MainActivity).changeBgColor(R.color.green)
        // toolbar title
        setToolbarTitle(R.string.title_recipes)

        setToolbarColor(R.color.green)
        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_search,
            object : BaseActivity.ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    showSearchBar(true)
                    setSearch()
                }
            })
        setUpToolbar(tabIndexToSelect)
        // setting up viewPager
        setUpViewPager()
        // binding viewpager with the tab layout
        //binding.tabs.setupWithViewPager(binding.viewpager)

        customizeTab(tabIndexToSelect)
        newTabLayout()
    }

    override fun getViewModel(): RecipeViewModel {
        viewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        /*inflater.inflate(R.menu.recipes_fragment_menu, menu)

        val item = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        //binding.searchView.setSuggestions(resources.getStringArray(R.array.query_suggestions))
        binding.searchView.setCursorDrawable(R.drawable.green_cursor)
        abc()*/
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setUpViewPager() {
        adapter = ViewPagerAdapterForTabs(childFragmentManager!!)
        adapter.addFragment(BreakfastFragment.newInstance(), getString(R.string.recipe))
        adapter.addFragment(LunchFragment.newInstance(), getString(R.string.lunch))
        adapter.addFragment(DinnerFragment.newInstance(), getString(R.string.dinner))
        binding.viewpager.adapter = adapter
        // selecting the default item based on the tab index received in intent
        binding.viewpager.currentItem = tabIndexToSelect
        binding.viewpager.offscreenPageLimit = 2
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.e("position", "" + position)
                Log.e("positionOffset", "" + positionOffset)
                Log.e("positionOffsetPixels", "" + positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                // setup toolbar again for selected tab
                setUpToolbar(position)
                // customize tab again based on selected tab
                //customizeTab(position, true)
            }
        })

        binding.viewpager.addOnPageChangeListener(ColorTabLayoutOnPageChangeListener(binding.tabLayout))

    }

    /*A common method change the bg and text of the toolbar based on the tab selected*/
    private fun decorateToolbar(title: Int, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color
        setToolbarColor(bgColor)
        // bgcolor of dummyView below tab layout
        binding.dummyView.setBackgroundColor(ContextCompat.getColor(context!!, bgColor))
        // tabs background color
        //binding.tabs.setBackgroundColor(ContextCompat.getColor(context!!, bgColor))
        binding.tabLayout.setBackgroundColor(ContextCompat.getColor(context!!, bgColor))
        // and setting status bar color if OS is >= Lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, bgColor)
        }
    }

    /*Customizing the tab with custom views*/
    private fun customizeTab(selectedPosition: Int, forUpdate: Boolean = false) {
        // setting the first tab custom view
        /*binding.tabs.getTabAt(0)?.customView = getCustomTabViewAt(0, selectedPosition, forUpdate)
        // setting the second tab custom view
        binding.tabs.getTabAt(1)?.customView = getCustomTabViewAt(1, selectedPosition, forUpdate)
        // setting the third tab custom view
        binding.tabs.getTabAt(2)?.customView = getCustomTabViewAt(2, selectedPosition, forUpdate)*/
    }


    private fun setUpToolbar(position: Int) {
        when (position) {
            0 -> {
                decorateToolbar(R.string.recipe, R.color.green)
            }
            1 -> {
                decorateToolbar(R.string.recipe, R.color.green)
            }
            2 -> {
                decorateToolbar(R.string.recipe, R.color.green)
            }
        }
    }

    /*getting custom tab view based on tab is selected or not*/
    private fun getCustomTabViewAt(tabIndex: Int, selectedPosition: Int, forUpdate: Boolean): View {
        // if the method is called when tab changes, then we will use the tabView as from tab.getView
        // or if method called from first time then we will inflate the custom view
        /*val tabView: View = if (forUpdate) {
            binding.tabs.getTabAt(tabIndex)?.customView!!
        } else {
            LayoutInflater.from(context!!).inflate(R.layout.custom_tab, null)
        }
        val llContainer = tabView.findViewById<LinearLayout>(R.id.llContainer)
        val ivTab = tabView.findViewById<ImageView>(R.id.ivTab)
        val tvTab = tabView.findViewById<TextView>(R.id.tvTab)

        //If this tab is selected, then we will show a view based on that
        //an icon with a lable and backgeound based on tab index
        if (selectedPosition == tabIndex) {
            when (tabIndex) {
                0 -> {
                    llContainer.setBackgroundResource(R.drawable.right_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_green_breakfast_tab)
                    tvTab.text = getString(R.string.breakfast)
                    tvTab.setTextColor(ContextCompat.getColor(context!!, R.color.green))
                }
                1 -> {
                    llContainer.setBackgroundResource(R.drawable.both_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_recipe_tab_green)
                    tvTab.text = getString(R.string.lunch)
                    tvTab.setTextColor(ContextCompat.getColor(context!!, R.color.green))
                }
                2 -> {
                    llContainer.setBackgroundResource(R.drawable.left_side_rounded_bg)
                    ivTab.setImageResource(R.drawable.ic_green_dinner_tab)
                    tvTab.text = getString(R.string.dinner)
                    tvTab.setTextColor(ContextCompat.getColor(context!!, R.color.green))
                }
            }
            tvTab.visibility = View.VISIBLE
        } else {
            // tab is not selected, then we will show only icon on transparent bg
            llContainer.setBackgroundResource(0)
            when (tabIndex) {
                0 -> {
                    ivTab.setImageResource(R.drawable.ic_white_breakfast_tab)
                }
                1 -> {
                    ivTab.setImageResource(R.drawable.ic_recipe_tab_white)
                }
                2 -> {
                    ivTab.setImageResource(R.drawable.ic_white_dinner_tab)
                }
            }
            tvTab.visibility = View.GONE
        }

        val layout = (binding.tabs.getChildAt(0) as LinearLayout).getChildAt(tabIndex)
        val layoutParams: LinearLayout.LayoutParams =
            layout.layoutParams as LinearLayout.LayoutParams
        // if tab is selected then we will assign weight 2 to it otherwise assign weight 1
        if (selectedPosition == tabIndex) {
            layoutParams.weight = 2f
        } else {
            layoutParams.weight = 1f
        }
        layout.layoutParams = layoutParams

        return tabView*/
        return View(context)
    }

    fun openHowToAddRecipeScreen() {
        context?.let {
            val subscriptionStatus = AppUtils.getUserSubscription(it)
            if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                AppUtils.startSubscriptionActivity(context)
            } else {
                startActivity(
                    WebViewActivity.newInstance(
                        context!!,
                        getString(R.string.lbl_privacy_policy),
                        AppConstants.PRIVACY_URL
                    )
                )
                AppUtils.startFromRightToLeft(context!!)
            }
        }
    }
    private fun setSearch() {
        val editText = getEditTextView()
        editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })



        editText?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val DRAWABLE_LEFT = 0
                val DRAWABLE_TOP = 1
                val DRAWABLE_RIGHT = 2
                val DRAWABLE_BOTTOM = 3

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        showSearchBar(false)
                        val mFragment = adapter.getItem(0)
                        (mFragment as BreakfastFragment).setSearchAction("")


                        val mFragment1 = adapter.getItem(1)
                        (mFragment1 as LunchFragment).setSearchAction("")

                        val mFragment2 = adapter.getItem(2)
                        (mFragment2 as DinnerFragment).setSearchAction("")
                        /*if (mFragment is BreakfastFragment) {

                            mFragment.setSearchAction("")
                        }*/
/*
                        if (mFragment is LunchFragment) {
                            mFragment.setSearchAction("")
                        }

                        if (mFragment is DinnerFragment) {
                            mFragment.setSearchAction("")
                        }*/
                    }
                }
                return false
            }
        })

        editText?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (editText.text.toString().isNotEmpty()) {
                        searchvalue = editText.text.toString()
                        val mFragment = adapter.getItem(binding.viewpager.currentItem)
                        if (mFragment is BreakfastFragment) {
                            mFragment.setSearchAction(searchvalue)
                        }
                        if (mFragment is LunchFragment) {
                            mFragment.setSearchAction(searchvalue)
                        }

                        if (mFragment is DinnerFragment) {
                            mFragment.setSearchAction(searchvalue)
                        }

                    }
                    return true
                }
                return false
            }
        })
    }

    private fun newTabLayout() {
        binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.breakfast), ContextCompat.getColor(context!!, R.color.white),
                resources.getDrawable(R.drawable.ic_white_breakfast_tab)))
        binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.lunch), ContextCompat.getColor(context!!, R.color.white),
                resources.getDrawable(R.drawable.ic_recipe_tab_white)))
        binding.tabLayout.addTab(
            ColorTabAdapter.createColorTab(binding.tabLayout,
                getString(R.string.dinner), ContextCompat.getColor(context!!, R.color.white),
                resources.getDrawable(R.drawable.ic_green_dinner_tab)))


        binding.tabLayout.selectedTabWidth = AppUtils.getScreenWidth(context!!)/2

        binding.tabLayout.getTabAt(0)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 0
        }
        binding.tabLayout.getTabAt(1)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 1
        }
        binding.tabLayout.getTabAt(2)?.tabView?.setOnClickListener {
            binding.viewpager.currentItem = 2
        }

        binding.tabLayout.selectedTabIndex = tabIndexToSelect
    }

}