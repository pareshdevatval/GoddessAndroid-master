package com.krystal.goddesslifestyle.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.MyAddressListAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.databinding.ActivityMyAddressListBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.MyAddressListModel
import javax.inject.Inject

class MyAddressListActivity : BaseActivity<MyAddressListModel>(),
    BaseBindingAdapter.ItemClickListener<MyAddressListResponse.Result?> {


    override fun onItemClick(view: View, data: MyAddressListResponse.Result?, position: Int) {
        when (view.id) {
            R.id.ivDelete -> {
                deleteItemPotion = position
                val params: HashMap<String, Any?> = HashMap()
                params[ApiParam.U_ADDRESS_ID] = data?.uaId
                mViewModel.callDeleteApi(params)

            }
            R.id.ivEdit -> {
                data?.let {
                    startActivityForResult(
                        EditAddressActivity.newInstance(
                            this,
                            data?.uaId!!,
                            data.uaAddressTitle!!,
                            data.uaPinCode!!,
                            data.uaHouseNo!!,
                            data.uaRoadAreaColony!!,
                            data.uaCity!!,
                            data.uaState!!,
                            data.uaLandmark,
                            data.uaCountry), 101
                    )
                }
            }
            else -> {
                if (from.equals(AppConstants.FROM_ORDER)) {
                    val address = data
                    val returnIntent = Intent()
                    returnIntent.putExtra(AppConstants.SELECTED_ADDRESS, address)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }
        }
    }

    private lateinit var binding: ActivityMyAddressListBinding
    private lateinit var mViewModel: MyAddressListModel

    var deleteItemPotion = 0
    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    var from = ""

    companion object {
        fun newInstance(context: Context, from: String = ""): Intent {
            val intent = Intent(context, MyAddressListActivity::class.java)
            intent.putExtra(AppConstants.FROM, from)
            return intent
        }
    }

    override fun getViewModel(): MyAddressListModel {
        mViewModel = ViewModelProvider(this).get(MyAddressListModel::class.java)
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
        requestsComponent.injectMyAddressListActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_address_list)

        intent?.let {
            from = it.getStringExtra(AppConstants.FROM)
        }

        init()
    }

    fun init() {
        mViewModel.setInjectable(apiService, prefs)


        binding.rvMyAddress.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        val myAddressListAdapter = MyAddressListAdapter()

        binding.rvMyAddress.adapter = myAddressListAdapter
        myAddressListAdapter.itemClickListener = this
        mViewModel.callMyAddressListApi()
        mViewModel.getMyAddressListResponse().observe(this, myAddressListObsrver)
        mViewModel.getDeleteAddressResponse().observe(this, deleteObsrver)
        setToolBar(getString(R.string.lbl_my_address), R.color.pink)
        /*add my address*/
        binding.viewAddress.btnAddAddress.setOnClickListener {
         addAddress()
        }
    }


    private fun setToolBar(title: String, bgColor: Int) {
        // setting toolbar title
        setToolbarTitle(title)
        // toolbar color

        setToolbarColor(bgColor)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })

        // toolbar right icon and its click listener
        setToolbarRightIcon(
            R.drawable.ic_plus_white,
            object : ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    addAddress()
                }
            })


    }

    /*observer my address list data*/
    private val myAddressListObsrver = Observer<MyAddressListResponse> {
        if (it.status && it.result.isNotEmpty()) {
            binding.rvMyAddress.visibility = View.VISIBLE
            binding.llNoData.visibility = View.GONE
            (binding.rvMyAddress.adapter as MyAddressListAdapter).clear()
            (binding.rvMyAddress.adapter as MyAddressListAdapter).setItem(it.result)
            (binding.rvMyAddress.adapter as MyAddressListAdapter).notifyDataSetChanged()
        } else {
            binding.rvMyAddress.visibility = View.VISIBLE
            binding.llNoData.visibility = View.VISIBLE
            AppUtils.startNotificationWaveAnimation(
                this,
                binding.viewAddress.wave,
                R.color.pink
            )

        }
    }
    /*observer delete address data*/
    private val deleteObsrver = Observer<BaseResponse> {
        if (it.status) {
            (binding.rvMyAddress.adapter as MyAddressListAdapter).removeItem(deleteItemPotion)
            (binding.rvMyAddress.adapter as MyAddressListAdapter).notifyItemChanged(deleteItemPotion)
            if ((binding.rvMyAddress.adapter as MyAddressListAdapter).items.size == 0) {
                binding.rvMyAddress.visibility = View.VISIBLE
                binding.llNoData.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getBooleanExtra("is_update", false)
                if (result) {
                    mViewModel.callMyAddressListApi()
                }
            }

        }
    }


    fun addAddress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 501)
                return
            }
        }

        startActivityForResult(AddAddressActivity.newInstance(this@MyAddressListActivity), 101)
    }
}
