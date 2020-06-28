package com.krystal.goddesslifestyle.ui.main_activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.Practice
import com.krystal.goddesslifestyle.databinding.TodayPracticeBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.community.PlayVideoActivity
import com.krystal.goddesslifestyle.utils.ApiContants
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.TodaysPracticeViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import zlc.season.rxdownload4.DEFAULT_SAVE_PATH
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.utils.safeDispose
import javax.inject.Inject


/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class TodaysPracticePagerFagment : BaseFragment<TodaysPracticeViewModel>(), View.OnClickListener {

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment
        */
        fun newInstance(): TodaysPracticePagerFagment {
            val bundle = Bundle()
            val fragment = TodaysPracticePagerFagment()
            fragment.arguments = bundle
            return fragment
        }
    }

    val PERMISSION_REQ_EXTERNAL_STORAGE = 11

    private lateinit var viewModel: TodaysPracticeViewModel
    private lateinit var binding: TodayPracticeBinding

    private var todayPracticeId: Int = -1

    private var videoUrl = ""

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    var practice: Practice? = null

    private var disposable: Disposable? = null

    override fun getViewModel(): TodaysPracticeViewModel {
        viewModel = ViewModelProvider(this).get(TodaysPracticeViewModel::class.java)
        return viewModel
    }

    override fun internetErrorRetryClicked() {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        requestsComponent.injectTodaysPracticePagerFragment(this)

        binding = TodayPracticeBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.mainLayout.clipToOutline = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInjectable(apiService, prefs)
        viewModel.getShareApiResponse().observe(viewLifecycleOwner, shareApiResponseObserver)
    }

    /*Accepts
    @calenderId : day id for which we have to show Data
    If user selected 9th of the month then in "calenderDayId", the id of 9th date will be passed
    This would be needed to retrieve the data of this date from db*/
    fun setCalenderDay(calenderDayId: Int) {
        // we can get today's practice id from currently selected calenderDay
        todayPracticeId = appDatabase.todaysPracticeDao().getTodayPracticeId(calenderDayId)
        /*And from today's practice id, We can get the object of Practice*/
        practice = appDatabase.practiceDao().getPractice(todayPracticeId)


        practice?.let { prac ->
            //videoUrl="http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            videoUrl = prac.practiceVideo
            binding.tvPracticieName.text = prac.practiceTitle
            binding.ivPracticieThumbnail.post {
                context?.let {
                    Log.e(
                        "PRACTICE", AppUtils.generateImageUrl(
                            prac.practiceImage, binding.ivPracticieThumbnail.width,
                            binding.ivPracticieThumbnail.height
                        )
                    )
                    AppUtils.loadImageThroughGlide(
                        it, binding.ivPracticieThumbnail,
                        AppUtils.generateImageUrl(
                            prac.practiceImage, binding.ivPracticieThumbnail.width,
                            binding.ivPracticieThumbnail.height
                        ),
                        R.drawable.ic_placeholder_rect
                    )
                }
            }
            /*Now, if Practice is not null, then go forward and look for the equipment used for this pratice
            * Getting equipment id based on practice*/
            val equipmentId = appDatabase.practiceEquipmentDao().getEquipmentId(prac.practiceId)
            /*and from equipment id, getting an object of the equipment*/
            val equipment = appDatabase.equipmentDao().getEquipment(equipmentId)


            binding.ivPracticeEquipment.post {
                context?.let {
                    if (equipment != null) {
                      /*  Log.e(
                            "EQUIPMENT", AppUtils.generateImageUrl(
                                equipment.equipmentImage,
                                binding.ivPracticeEquipment.width,
                                binding.ivPracticeEquipment.height
                            )
                        )*/
                        AppUtils.loadImageThroughGlide(
                            it, binding.ivPracticeEquipment,
                            AppUtils.generateImageUrl(
                                equipment.equipmentImage,
                                binding.ivPracticeEquipment.width,
                                binding.ivPracticeEquipment.height
                            ),
                            R.drawable.ic_placeholder_square
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        if (todayPracticeId == -1) {
            return
        }
        when (v?.id) {
            R.id.shareLayout -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(context, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            if (practice?.practiceVideo != null && practice?.practiceVideo!!.isNotBlank()) {
                                AppUtils.shareContent(it, practice?.practiceVideo!!)
                                callShareApi()
                            } else {
                                if (practice?.practiceImage != null && practice?.practiceImage!!.isNotBlank()) {
                                    val imgUrl =
                                        AppUtils.generateImageUrl(practice?.practiceImage!!, 0, 0)
                                    AppUtils.shareContent(it, imgUrl)
                                } else if (practice?.practiceDescription != null) {
                                    AppUtils.shareContent(it, practice?.practiceDescription!!)
                                } else {
                                    AppUtils.shareContent(
                                        it,
                                        "Share from Goddess LifeStyle Android App"
                                    )
                                }
                                callShareApi()
                            }
                        }
                    }
                }

                /*context?.let {
                    if (practice?.practiceVideo != null && practice?.practiceVideo!!.isNotBlank()) {
                        AppUtils.shareContent(it, practice?.practiceVideo!!)
                    } else {
                        if (practice?.practiceImage != null && practice?.practiceImage!!.isNotBlank()) {
                            val imgUrl = AppUtils.generateImageUrl(practice?.practiceImage!!, 0, 0)
                            AppUtils.shareContent(it, imgUrl)
                        } else if (practice?.practiceDescription != null) {
                            AppUtils.shareContent(it, practice?.practiceDescription!!)
                        } else {
                            AppUtils.shareContent(it, "Share from Goddess LifeStyle Android App")
                        }
                        callShareApi()
                    }

                    *//*if(practice?.practiceImage != null && practice?.practiceImage!!.isNotBlank()) {
                        val imgUrl = AppUtils.generateImageUrl(practice?.practiceImage!!, 0, 0)
                        AppUtils.shareContent(it, imgUrl)
                    } else if(practice?.practiceDescription != null) {
                        AppUtils.shareContent(it, practice?.practiceDescription!!)
                    } else {
                        AppUtils.shareContent(it, "Share from Goddess LifeStyle Android App")
                    }
                    callShareApi()*//*
                }*/
            }
            R.id.btn_complete_practice -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (subscriptionStatus == AppConstants.BASIC_SUBSCRIPTION) {
                            //AppUtils.startSubscriptionActivity(context)
                            AppUtils.startSubscriptionActivity(context, true)
                        } else if (subscriptionStatus == AppConstants.PREMIUM_SUBSCRIPTION) {
                            val params = HashMap<String, String>()
                            params[ApiContants.ACTIVITY_ID] = todayPracticeId.toString()
                            params[ApiContants.EARNED_POINTS] =
                                ApiContants.COMPLETE_PRACTICE_POINTS.toString()
                            params[ApiContants.TYPE] = ApiContants.SHARE_TYPE_COMPLETE_PRACTICE
                            viewModel.callShareApi(params, true)
                        }
                    }
                }
            }
            R.id.ivPlay -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        //AppUtils.showToast(it, "Under Development")
                        //AppUtils.showToast(it, "Playing test video")
                        startActivity(PlayVideoActivity.newInstance(it, videoUrl))
                    }

                }
            }
            R.id.ll_download -> {
                context?.let {
                    val subscriptionStatus = AppUtils.getUserSubscription(it)
                    if (subscriptionStatus == AppConstants.NO_SUBSCRIPTION) {
                        AppUtils.startSubscriptionActivity(context)
                    } else {
                        if (videoUrl.isNotEmpty()) {
                            if (ContextCompat.checkSelfPermission(
                                    context!!,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                // ask for permission
                                // Permission is not granted
                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(
                                        activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                ) {
                                    // Show an explanation to the user *asynchronously* -- don't block
                                    // this thread waiting for the user's response! After the user
                                    // sees the explanation, try again to request the permission.
                                    showRationaleDialogForStorage()
                                } else {
                                    // No explanation needed, we can request the permission.
                                    ActivityCompat.requestPermissions(
                                        activity!!,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        PERMISSION_REQ_EXTERNAL_STORAGE
                                    )
                                }

                            } else {
                                // permission already granted
                                downloadVideoFile()
                            }

                        } else {
                            AppUtils.showToast(it, getString(R.string.msg_video_not_available))
                        }
                    }
                }
            }
        }
    }

    private fun showRationaleDialogForStorage() {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Permissions")
        builder.setMessage(
            "Storage permission is required to download the video." +
                    "Please allow the permission"
        )

        builder.setPositiveButton("OK") { dialog, p1 ->
            dialog.dismiss()
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQ_EXTERNAL_STORAGE
            )
        }

        builder.setPositiveButton("CANCEL") { dialog, p1 ->
            dialog.dismiss()
        }
    }

    private fun downloadVideoFile() {

        DEFAULT_SAVE_PATH =
            context!!.getExternalFilesDir(null)!!.path + "/." + getString(R.string.app_name)

        if (!videoUrl.file().exists()) {
            disposable = videoUrl.download()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        AppUtils.logE("Progress", it.percentStr())
                    },
                    onComplete = {
                        AppUtils.showToast(context!!, getString(R.string.download_success))
                        startActivity(
                            PlayVideoActivity.newInstance(
                                context!!,
                                videoUrl.file().path
                            )
                        )
                    },
                    onError = {
                        Log.e("error", it.message!!)
                    }
                )
        } else {
            startActivity(PlayVideoActivity.newInstance(context!!, videoUrl.file().path))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                downloadVideoFile()
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.

            }
            return
        }
    }

    private fun callShareApi() {
        val params = HashMap<String, String>()
        params[ApiContants.ACTIVITY_ID] = todayPracticeId.toString()
        params[ApiContants.EARNED_POINTS] = ApiContants.SHARE_PRACTICE_POINTS.toString()
        params[ApiContants.TYPE] = ApiContants.SHARE_TYPE_PRACTICE
        viewModel.callShareApi(params, false)
    }

    private val shareApiResponseObserver = Observer<BaseResponse> {
        if (it.status) {
            //AppUtils.showToast(context!!, it.message)
        }
    }

    override fun onDestroyView() {
        if (disposable != null) {
            disposable.safeDispose()
        }
        super.onDestroyView()
    }

}