package com.krystal.goddesslifestyle.ui.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.Scroller
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.CommunityOpinionCommentAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.response.CommentData
import com.krystal.goddesslifestyle.data.response.CommunityOpinionCommentListResponse
import com.krystal.goddesslifestyle.databinding.ActivityCommentsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.CommentsViewModel
import javax.inject.Inject


class ActivityComments : BaseActivity<CommentsViewModel>(){


    private lateinit var mViewModel: CommentsViewModel
    private lateinit var binding: ActivityCommentsBinding
    private lateinit var adapter: CommunityOpinionCommentAdapter
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs
    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    var commentCounts = 0

    companion object {
        fun newInstance(context: Context, opinionId: Int, commentCount: String): Intent {
            val intent = Intent(context, ActivityComments::class.java)
            intent.putExtra(AppConstants.OPINION_ID, opinionId)
            intent.putExtra(AppConstants.COMMENT_COUNT, commentCount)
            return intent
        }
    }

    private val opinionId: Int by lazy {
        intent.getIntExtra(AppConstants.OPINION_ID, 0)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectCommentsActivity(this)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_comments)
        super.onCreate(savedInstanceState)
        init()

        loadMore()
    }
    private fun init(){
        binding.commentActivity=this
        mViewModel.setInjectable(apiService, prefs)
        //setToolbarTitle(getString(R.string.lbl_comments))

        var commentCount: String? = null

        intent?.let {
            commentCount = it.getStringExtra(AppConstants.COMMENT_COUNT)
            if(commentCount != null) {
                commentCounts = commentCount!!.toInt()
            }
        }

        if(commentCount != null && commentCount!!.isNotBlank() && commentCount!!.toInt() != 0) {
            setToolbarTitle(getString(R.string.lbl_comments) + "(" + commentCount + ")")
        } else {
            setToolbarTitle(getString(R.string.lbl_comments))
        }

        setToolbarColor(R.color.violet)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.violet)
        }

        setToolbarLeftIcon(
            R.drawable.ic_back,
            object : ToolbarLeftImageClickListener {
                override fun onLeftImageClicked() {
                    onBackPressed()
                }
            })

        setCommentAdapter()
        mViewModel.getCommunityOpinionCommentResponse().observe(this, commentListObserver)
        mViewModel.getAddCommentResponse().observe(this, addCommentObserver)
        mViewModel.callGetCommunityCommentListAPI(opinionId)

        binding.edtComment.setScroller(Scroller(this))
        binding.edtComment.maxLines = 5
        binding.edtComment.isVerticalScrollBarEnabled = true
        binding.edtComment.movementMethod = ScrollingMovementMethod()
    }

    private val commentListObserver = Observer<CommunityOpinionCommentListResponse> {
        if (it.status) {
            if (CURRENT_PAGE == 1) {
                TOTAL_PAGE = it.pagination.lastPage
                if (it.result.isNotEmpty()) {
                    binding.edtComment.setText("")
                    adapter.setItem(it.result as ArrayList<CommentData?>)
                    binding.clNoOpinion.visibility = View.GONE
                    binding.clCommentDataView.visibility = View.VISIBLE
                    binding.clMainView.setBackgroundColor(resources.getColor(R.color.violet))
                } else {
                    setNoOpinions()
                }
            } else {
                adapter.removeLoadingFooter(true)
                val adapterCount = adapter.itemCount
                adapter.removeItem(adapterCount - 1)

                val sizeList =
                    adapter.itemCount
                adapter.addItems(it.result as ArrayList<CommentData?>)
                adapter.notifyItemRangeInserted(sizeList,it.result.size)
            }
        } else {
            if(CURRENT_PAGE != 1) {
                setNoOpinions()
            }
        }
    }

    private val addCommentObserver = Observer<BaseResponse> {
        if(it.status) {
            binding.edtComment.setText("")
            commentCounts += 1
            setToolbarTitle(getString(R.string.lbl_comments) + "(" + commentCounts + ")")

            CURRENT_PAGE = 1
            isLoading = true
            mViewModel.callGetCommunityCommentListAPI(this.opinionId, CURRENT_PAGE)

        } else {
            AppUtils.showToast(this, it.message)
        }
    }
    override fun getViewModel(): CommentsViewModel {
        mViewModel = ViewModelProvider(this).get(CommentsViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun setCommentAdapter() {
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        adapter = CommunityOpinionCommentAdapter()
        binding.rvComments.setHasFixedSize(true)
        binding.rvComments.adapter = adapter
    }

    private fun setNoOpinions() {
        binding.clNoOpinion.visibility = View.VISIBLE
        binding.clCommentDataView.visibility = View.GONE
        binding.clMainView.setBackgroundColor(resources.getColor(R.color.white))
        AppUtils.setWaveAnimation(this, R.color.violet, binding.wave)
    }

    fun sendComment() {
        mViewModel.checkCommentValidation(this, opinionId, binding.edtComment.text.toString(), CURRENT_PAGE)
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1
    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvComments.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvComments.layoutManager as LinearLayoutManager,
                true
            ) {
            override fun loadMoreItems() {

                /*If we have load all total pages then no need to go for next*/
                if (TOTAL_PAGE != CURRENT_PAGE) {
                    /*checking for loading flag to prevent too many calls at same time*/
                    if (isLoading) {
                        /*switching the value of loading flag*/
                        isLoading = false
                        /*increment the current page count*/
                        CURRENT_PAGE += 1

                        /*Adding a dummy(null) item to the list to show a footer progress bar*/
                        (binding.rvComments.adapter as CommunityOpinionCommentAdapter).addItem(null)
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvComments.adapter as CommunityOpinionCommentAdapter).itemCount
                        (binding.rvComments.adapter as CommunityOpinionCommentAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvComments.adapter as CommunityOpinionCommentAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callGetCommunityCommentListAPI(opinionId, pageNo = CURRENT_PAGE, showProgress = false)
                    }
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGE
            }

            override fun isLastPage(): Boolean {
                return false
            }

            override fun isLoading(): Boolean {
                return false
            }

        })

    }
}
