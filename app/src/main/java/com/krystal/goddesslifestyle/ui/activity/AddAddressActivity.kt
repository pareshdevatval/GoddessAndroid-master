package com.krystal.goddesslifestyle.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.georeminder.utils.validator.ValidationErrorModel
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.databinding.ActivityAddAddressBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.MyLocationProvider
import com.krystal.goddesslifestyle.viewmodels.AddAddressModel
import java.util.*
import javax.inject.Inject

class AddAddressActivity : BaseActivity<AddAddressModel>(), MyLocationProvider.MyLocationListener,
    View.OnClickListener {


    private lateinit var binding: ActivityAddAddressBinding
    private lateinit var mViewModel: AddAddressModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase
    var locationProvider: MyLocationProvider? = null
    var mLongitude: Double = 0.0
    var mLatitude: Double = 0.0
    var isUpdate: Boolean = false

    var isCalledForResult = false


    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, AddAddressActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): AddAddressModel {
        mViewModel = ViewModelProvider(this).get(AddAddressModel::class.java)
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
        requestsComponent.injectAddAddressActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)

        if (callingActivity != null) {
            isCalledForResult = true
        }

        init()
    }

    fun init() {
        mViewModel.setInjectable(apiService, prefs)
        setToolBar(getString(R.string.lbl_add_address), R.color.pink)
        binding.tvUseMayCurLoc.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.etCity.setOnClickListener(this)
        mViewModel.getAddAddressResponse().observe(this, addAddressObserv)
        mViewModel.validationResponse().observe(this, validationObserv)
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

    }

    /*observer address add data*/
    private val addAddressObserv = Observer<BaseResponse> {
        if (it.status) {
            isUpdate = true
            closeActivity()
        } else {
            AppUtils.showSnackBar(binding.btnSave, it.message)
        }
    }

    /*observer validation */
    private val validationObserv = Observer<ValidationErrorModel> {
        AppUtils.showSnackBar(binding.btnSave, getString(it.msg))
    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        when (v?.id) {
            R.id.tvUseMayCurLoc -> {
                //get current address
                showProgress()
                getLocation()
            }
            R.id.btnSave -> {
                //api call
                mViewModel.validation(
                    AppUtils.getText(binding.etAddAddress),
                    AppUtils.getText(binding.etPinCode),
                    AppUtils.getText(binding.etHouseNo),
                    AppUtils.getText(binding.etRoadName),
                    AppUtils.getText(binding.etCity),
                    AppUtils.getText(binding.etState),
                    AppUtils.getText(binding.etLandMark),
                    AppUtils.getText(binding.etCountry)
                )
            }

            R.id.etCity -> {
                startActivityForResult(
                    SelectCityStateActivity.newInstance(this),
                    AppConstants.SELECT_CITY_STATE
                )
            }

        }

    }

    /*get current lat/long*/
    override fun onLocationReceived(location: Location?) {

        location?.let {
            Log.w("LATITUDE", "" + it.latitude)
            Log.w("LONGITUDE", "" + it.longitude)
            mLatitude = it.latitude
            mLongitude = it.longitude
            getAddress()
            locationProvider?.stopLocationUpdates()

            // When we need to get location again, then call below line
            //    locationProvider?.startGettingLocations()


        }
    }

    /*get current address*/
    private fun getAddress() {
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        addresses = geocoder.getFromLocation(
            mLatitude,
            mLongitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city = addresses[0].getLocality()
        val state = addresses[0].getAdminArea()
        val country = addresses[0].getCountryName()
        val postalCode = addresses[0].getPostalCode()
        val knownName = addresses[0].getFeatureName() // Only if available else return NULL

        binding.etPinCode.setText(postalCode)
        if (city.isNotBlank() && state.isNotBlank() && country.isNotBlank()) {
            binding.etCity.setText(city)
            binding.etState.setText(state)
            binding.etCountry.setText(country)
        } else {
            binding.etCity.setText("")
            binding.etState.setText("")
            binding.etCountry.setText("")
        }
        binding.etRoadName.setText(knownName)
        hideProgress()
    }

    fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    fun closeActivity() {
        val returnIntent = Intent()
        returnIntent.putExtra("is_update", isUpdate)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyLocationProvider.REQUEST_LOCATION_SETTINGS) {
            locationProvider?.onActivityResult(requestCode, resultCode, intent)
        }

        if (requestCode == AppConstants.SELECT_CITY_STATE && resultCode == Activity.RESULT_OK) {
            val city: String? = data!!.getStringExtra(AppConstants.SELECTED_CITY)
            val state: String? = data.getStringExtra(AppConstants.SELECTED_STATE)
            val country: String? = data.getStringExtra(AppConstants.SELECTED_COUNTRY)

            binding.etCity.setText(city)
            binding.etState.setText(state)
            binding.etCountry.setText(country)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MyLocationProvider.LOCATION_PERMISSION_REQUEST_CODE) {
            locationProvider?.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
            }
        }
    }
}
