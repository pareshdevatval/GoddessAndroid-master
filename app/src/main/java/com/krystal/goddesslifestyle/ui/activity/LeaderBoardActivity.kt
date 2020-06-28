package com.krystal.goddesslifestyle.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.LeaderBordAdapter
import com.krystal.goddesslifestyle.adapter.NotificationAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.LeaderBoradModel
import com.krystal.goddesslifestyle.data.response.LeaderBoardResponse
import com.krystal.goddesslifestyle.databinding.ActivityLeaderBoardBinding
import com.krystal.goddesslifestyle.databinding.LayoutLeaderBoardViewBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.LeaderBoardModel
import okhttp3.internal.notifyAll
import javax.inject.Inject

class LeaderBoardActivity : BaseActivity<LeaderBoardModel>() {

    private lateinit var binding: ActivityLeaderBoardBinding
    private lateinit var mViewModel: LeaderBoardModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, LeaderBoardActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): LeaderBoardModel {
        mViewModel = ViewModelProvider(this).get(LeaderBoardModel::class.java)
        return mViewModel

    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {

        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectLeaderBoardActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leader_board)
        mViewModel.setInjectable(apiService, prefs)

        init()
    }

    fun init() {
        binding.rvLeaderBord.layoutManager = LinearLayoutManager(this)
        val leaderBordAdapter = LeaderBordAdapter()
        binding.rvLeaderBord.adapter = leaderBordAdapter
        mViewModel.getLeaderBoardResponse().observe(this, leaderBordObserver)
        mViewModel.callLeaderBoardApi()
        setToolBar(getString(R.string.lbl_leader_boader), R.color.pink)


    }
    val list:ArrayList<LeaderBoradModel?> = ArrayList()

    /*observer leader board data*/
    private val leaderBordObserver = Observer<LeaderBoardResponse> {
        if(it.status){
            val response = it.result
            /*set 1 potion data*/
            AppUtils.loadImages(this,  response[0].uImage, binding.ivGoddess, R.drawable.ic_placeholder_square)
            AppUtils.setImages(response[0].totalPoint.toInt(),binding.ivCrown,this,true)
            binding.tvName.text=response[0].uUserName
            binding.tvPoint.text=response[0].totalPoint
            /*set 2-4 potion data*/
            for (i in 1 until it.result.size){
                when (i) {
                    1 -> list.add(LeaderBoradModel("2nd",it.result[i].uUserName,it.result[i].uImage,it.result[i].totalPoint))
                    2 -> list.add(LeaderBoradModel("3rd",it.result[i].uUserName,it.result[i].uImage,it.result[i].totalPoint))
                    3 -> list.add(LeaderBoradModel("4th",it.result[i].uUserName,it.result[i].uImage,it.result[i].totalPoint))
                }
            }
            (binding.rvLeaderBord.adapter as LeaderBordAdapter).setItem(list)
            (binding.rvLeaderBord.adapter as LeaderBordAdapter).notifyDataSetChanged()

        binding.mainLayout.visibility= View.VISIBLE
        }


    }



    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color


        setToolbarColor(bgColor)
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

    }

}
