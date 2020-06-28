package com.krystal.goddesslifestyle.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.CityStateAdepter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.data.model.CityStateModel
import com.krystal.goddesslifestyle.data.response.MyAddressListResponse
import com.krystal.goddesslifestyle.databinding.ActivitySelectCityStateBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.AppConstants.SELECTED_CITY
import com.krystal.goddesslifestyle.utils.AppConstants.SELECTED_COUNTRY
import com.krystal.goddesslifestyle.utils.AppConstants.SELECTED_STATE
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.SelectCityStateViewModel

class SelectCityStateActivity : BaseActivity<SelectCityStateViewModel>(), BaseBindingAdapter.ItemClickListener<CityStateModel?>{

    private lateinit var mViewModel: SelectCityStateViewModel
    private lateinit var binding: ActivitySelectCityStateBinding
    lateinit var token: AutocompleteSessionToken
    lateinit var placesClient: PlacesClient
    val adepter = CityStateAdepter()

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, SelectCityStateActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
            .builder()
            .networkComponent(getNetworkComponent())
            .localDataComponent(getLocalDataComponent())
            .build()
        requestsComponent.injectSelectCityStateActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_city_state)
        init()
    }

    override fun getViewModel(): SelectCityStateViewModel {
        mViewModel = ViewModelProvider(this).get(SelectCityStateViewModel::class.java)
        return mViewModel
    }

    private fun init(){
        setToolBar()
        initNewPlacesApi()
    }

    private fun setToolBar(){
        setToolbarTitle("Select your city")
        setToolbarColor(R.color.pink)

        // toolbar left icon and its click listener
        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })
    }

    private fun initNewPlacesApi() {
        Places.initialize(this, resources.getString(R.string.places_api_key))
        placesClient = Places.createClient(this)
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        token = AutocompleteSessionToken.newInstance()


        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                findCities(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun findCities(query: String) {

        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.CITIES)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            for (prediction in response.autocompletePredictions) {
                //Log.i(TAG, prediction.placeId)
                Log.e("DATA", prediction.getPrimaryText(null).toString())
                Log.e("DATA", prediction.getSecondaryText(null).toString())
                Log.e("DATA", prediction.getFullText(null).toString())
            }
            //intSpinnerBillingCity2(binding.etCity, response.autocompletePredictions)
            setAutoCompleteList(response.autocompletePredictions)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("DATA", "Place not found: " + exception.localizedMessage)
            }
        }
    }

    private fun setAutoCompleteList(list: List<AutocompletePrediction?>) {
        Log.e("SIZE", "" + list.size)
        val citySateList = ArrayList<CityStateModel?>()
        /* val cityNamesList = ArrayList<String>()
         val stateNamesList = ArrayList<String>()*/

        if (list.isNotEmpty()) {
            for (i in list.indices) {
                list[i]?.let {
                    val city = it.getPrimaryText(null).toString()
                    val state = it.getSecondaryText(null).toString().split(",")[0]
                    var country=""
                    if (it.getSecondaryText(null).toString().contains(",")){
                        country=it.getSecondaryText(null).toString().split(",")[1]
                    }else{
                        country=state
                    }
                    val cityStateModel = CityStateModel(city, state, country)
                    citySateList.add(cityStateModel)
                }
            }
        } else {
            citySateList.clear()
            adepter.items.clear()
            adepter.notifyDataSetChanged()
        }
        citySateList.let {
            binding.rvCityState.layoutManager = LinearLayoutManager(this)
            binding.rvCityState.adapter = adepter
            adepter.setItem(it)
            adepter.notifyDataSetChanged()
            adepter.itemClickListener=this
        }
    }
    override fun internetErrorRetryClicked() {
        TODO("Not yet implemented")
    }

    override fun onItemClick(view: View, data: CityStateModel?, position: Int) {
        val returnIntent = Intent()
        returnIntent.putExtra(SELECTED_CITY, data!!.city)
        returnIntent.putExtra(SELECTED_STATE, data.state)
        returnIntent.putExtra(SELECTED_COUNTRY, data.country)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

}
