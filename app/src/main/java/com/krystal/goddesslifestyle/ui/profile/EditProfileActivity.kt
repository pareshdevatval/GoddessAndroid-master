package com.krystal.goddesslifestyle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.georeminder.utils.filePick.FilePickUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.krystal.goddesslifestyle.BuildConfig
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.EditProfileResponse
import com.krystal.goddesslifestyle.databinding.ActivityEditProfileBinding
import com.krystal.goddesslifestyle.databinding.DialogImageSelectionBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.EditProfileModel
import javax.inject.Inject
import android.app.Activity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.os.SystemClock


class EditProfileActivity : BaseActivity<EditProfileModel>(), View.OnClickListener {
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        v?.let {
            when (v.id) {
                com.krystal.goddesslifestyle.R.id.ivCameraIcon -> {
                    showImageSelectionDialog()
                }
                com.krystal.goddesslifestyle.R.id.tvSelectCamera -> {
                    filePickUtils?.requestImageCamera(
                        AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
                com.krystal.goddesslifestyle.R.id.tvSelectGallery -> {
                    filePickUtils?.requestImageGallery(
                        AppConstants.PICK_IMAGE_GALLERY_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
                com.krystal.goddesslifestyle.R.id.ivBack -> {
                    onBackPressed()
                }
                com.krystal.goddesslifestyle.R.id.btnSave -> {
                    /*check validation */
                    if (binding.edtName.text.toString().isEmpty()) {
                        AppUtils.showSnackBar(binding.btnSave,"Please enter User name")
                        return
                    }
                    val selectedId = binding.radioGroup.checkedRadioButtonId

                    val radioButton = findViewById<View>(selectedId) as RadioButton

                    Log.e("GENDER", "--->" + radioButton.text)
                    val param:HashMap<String,String?> = HashMap()
                    param[ApiParam.USER_NAME]=AppUtils.getText(binding.edtName)
                    param[ApiParam.USER_EMAIL]=AppUtils.getText(binding.edtEmail)
                    param[ApiParam.USER_GENDER]=if(radioButton.text=="Male"){"1"}else{"2"}
                    //api call
                    mViewModel.callEditProfileApi(param)
                    Log.e("PARAMS","-->"+param)
                }
                else ->{

                }
            }
        }
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var mViewModel: EditProfileModel


    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    /*Database */
    @Inject
    lateinit var appDatabase: AppDatabase
    private var isUpdate:Boolean=false
    private var filePickUtils: FilePickUtils? = null
    private lateinit var mImageSelectionDialog: BottomSheetDialog

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            return intent
        }
    }

    override fun getViewModel(): EditProfileModel {
        mViewModel = ViewModelProvider(this).get(EditProfileModel::class.java)
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
        requestsComponent.injectEditProfileActivity(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            com.krystal.goddesslifestyle.R.layout.activity_edit_profile
        )
        mViewModel.setInjectable(apiService, prefs)
        init()
    }

    override fun onBackPressed() {
        closeActivity()
        super.onBackPressed()
    }
    fun init() {
        mViewModel.getEditProfileResponse().observe(this,editProfileObserve)
        filePickUtils = FilePickUtils(this, mOnFileChoose)
        binding.ivCameraIcon.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        binding.ivCameraIcon.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                com.krystal.goddesslifestyle.R.drawable.ic_camera_pink
            )
        )
        setData()
    }
/*observer edit profile data*/
    private val editProfileObserve= Observer<EditProfileResponse>{
        if(it.status){

            AppUtils.showToast(this, ""+it.message)
            val userResponse=prefs.userDataModel
            userResponse?.result?.uImage=it.result.uImage
            userResponse?.result?.uUserName=it.result.uUserName
            userResponse?.result?.uMobileNumber=it.result.uMobileNumber
            userResponse?.result?.uGender=it.result.uGender
            userResponse?.result?.uPoints=it.result.uPoints
            userResponse?.result?.uReferralCode=it.result.uReferralCode
            prefs.userDataModel=userResponse
            isUpdate=true
            closeActivity()
        }else{
            AppUtils.showSnackBar(binding.btnSave,it.message)
        }

    }

    private fun setData() {
        //set data
        val userData = prefs.userDataModel?.result
        userData?.let {
            Glide.with(this)
                .load(BuildConfig.IMAGE_BASE_URL + "" + userData.uImage)
                .placeholder(com.krystal.goddesslifestyle.R.drawable.ic_placeholder_square)
                .into(binding.ivPlaceholder)
            binding.edtName.setText(userData.uUserName)
            binding.edtEmail.setText(userData.uEmail)
            binding.edtContact.setText(userData.uMobileNumber)
            if(userData.uGender==1 || userData.uGender=="1"||userData.uGender==1.0){
                binding.rbMale.isChecked=true
            }else{
                binding.rbFemale.isChecked=true
            }
        }
    }

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Glide.with(this@EditProfileActivity).load(fileUri).into(binding.ivPlaceholder)
            mViewModel.userImages = fileUri

        }
    }

    private fun showImageSelectionDialog() {
        mImageSelectionDialog = BottomSheetDialog(this)
        val dialogImageSelectionBinding = DialogImageSelectionBinding.inflate(layoutInflater)
        mImageSelectionDialog.setContentView(dialogImageSelectionBinding.root)

        dialogImageSelectionBinding.tvSelectCamera.setOnClickListener(this)
        dialogImageSelectionBinding.tvSelectGallery.setOnClickListener(this)
        mImageSelectionDialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        filePickUtils?.onActivityResult(requestCode, resultCode, intent)

    }
    fun closeActivity(){
        val returnIntent = Intent()
        returnIntent.putExtra("is_update", isUpdate)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePickUtils?.onRequestPermissionsResult(
            requestCode,
            permissions as Array<String>,
            grantResults
        )
    }

}
