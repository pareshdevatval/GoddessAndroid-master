package com.krystal.goddesslifestyle.ui.community

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.Scroller
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.georeminder.utils.filePick.FilePickUtils

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.adapter.GoddessCommunityOpinionsAdapter
import com.krystal.goddesslifestyle.adapter.OpinionImageAdapter
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.custom_views.PaginationScrollListenerLinear
import com.krystal.goddesslifestyle.data.ApiService
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.model.ImageData
import com.krystal.goddesslifestyle.data.response.CommunityOpinionResponse
import com.krystal.goddesslifestyle.data.response.CommunityOpinionResult
import com.krystal.goddesslifestyle.data.response.LikeOpinionResponse
import com.krystal.goddesslifestyle.databinding.DialogImageSelectionBinding
import com.krystal.goddesslifestyle.databinding.FragmentCommunityDetailsBinding
import com.krystal.goddesslifestyle.di.component.DaggerNetworkLocalComponent
import com.krystal.goddesslifestyle.di.component.NetworkLocalComponent
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE
import com.krystal.goddesslifestyle.utils.AppConstants.PICK_IMAGE_GALLERY_REQUEST_CODE
import com.krystal.goddesslifestyle.utils.AppConstants.PICK_VIDEO_CAMERA_REQUEST_CODE
import com.krystal.goddesslifestyle.utils.AppConstants.PICK_VIDEO_GALLERY_REQUEST_CODE
import com.krystal.goddesslifestyle.utils.AppConstants.VIDEO_SIZE
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.GoddessCommunityOpinionsViewModel
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import javax.inject.Inject

/**
 * Created by imobdev on 31/3/20
 */
class GoddessCommunityOpinionsFragment : BaseFragment<GoddessCommunityOpinionsViewModel>(),
    BaseBindingAdapter.ItemClickListener<CommunityOpinionResult?>, View.OnClickListener,
    OpinionImageAdapter.OpinionImageListener {

    private lateinit var selectedData: CommunityOpinionResult
    private var selectedDataPosition = 0
    private lateinit var binding: FragmentCommunityDetailsBinding
    private lateinit var mViewModel: GoddessCommunityOpinionsViewModel
    private lateinit var adapter: GoddessCommunityOpinionsAdapter
    private lateinit var imageAdapter: OpinionImageAdapter
    private lateinit var mImageSelectionDialog: BottomSheetDialog
    private var filePickUtils: FilePickUtils? = null

    // private var seletedImageList = arrayListOf<String>()
    private var seletedImageArr = arrayListOf<ImageData>()
    private var isSelecteVideo = false
    private var isEdit = false
    private var editId = 0
    private lateinit var captureVideoFileName: Uri
    private var deleteMedia = ""

    /*Injecting prefs from DI*/
    @Inject
    lateinit var prefs: Prefs

    /*Injecting apiService*/
    @Inject
    lateinit var apiService: ApiService

    companion object {
        /*A static method to invoke this fragment class
        * Pass the required parameters in argument that are necessary to open this fragment*/
        fun newInstance(): GoddessCommunityOpinionsFragment {
            val bundle = Bundle()
            val fragment = GoddessCommunityOpinionsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val requestsComponent: NetworkLocalComponent =
            DaggerNetworkLocalComponent.builder().networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent()).build()
        requestsComponent.injectCommunityDetailsFragment(this)
        binding = FragmentCommunityDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        mViewModel.setInjectable(apiService, prefs)
        binding.communityOpinion = this
        init()
    }

    private fun init() {
        activity?.let {
            filePickUtils = FilePickUtils(this, mOnFileChoose)
        }
        (activity as MainActivity).changeBgColor(R.color.violet)
        initToolbar()
        setAdapter()
        setOpinionImageAdapter()
        mViewModel.getCommunityOpinionsResponse().observe(viewLifecycleOwner, opinionsObserver)
        mViewModel.getCommunityOpinionsLikeUnlikeResponse()
            .observe(viewLifecycleOwner, opinionsLikeUnlikeObserver)
        mViewModel.callGetGoddessCommunityOpinions()
        mViewModel.getAddOpinionResponseResponse()
            .observe(viewLifecycleOwner, addCommunityOpinionObserver)
        mViewModel.getDeleteOpinionResponse().observe(viewLifecycleOwner, deleteOpinionobserver)
        mViewModel.getCompressVideoResponse().observe(viewLifecycleOwner, videoCompressResponse)


        binding.tvPostDesc.setScroller(Scroller(context))
        binding.tvPostDesc.maxLines = 5
        binding.tvPostDesc.isVerticalScrollBarEnabled = true
        binding.tvPostDesc.movementMethod = ScrollingMovementMethod()

        setTextCount()


        loadMore()
    }

    /**
     * File chooser Listener
     */
    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            AppUtils.logE(fileUri)
            setImageList(fileUri)
        }
    }


    private fun initToolbar() {
        setToolbarTitle(R.string.title_goddess_community)
        setToolbarColor(R.color.violet)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context!!, R.color.violet)
        }
        setToolbarRightIcon(
            R.drawable.ic_write_white,
            object : BaseActivity.ToolbarRightImageClickListener {
                override fun onRightImageClicked() {
                    if (prefs.isLoggedIn) {
                        isEdit = false
                        if (binding.clWriteView.isVisible) {
                            binding.clWriteView.visibility = View.INVISIBLE
                            binding.clPostView.visibility = View.GONE
                        } else {
                            binding.clWriteView.visibility = View.VISIBLE
                            binding.clPostView.visibility = View.VISIBLE
                            imageAdapter.notifyDataSetChanged()
                        }
                        hideToolbarRightIcon()
                    } else {
                        context?.let { startActivity(LoginActivity.newInstance(it, "")) }
                    }

                }
            })

        setToolbarLeftIcon(
            R.drawable.ic_recipe_info_white_tab,
            object : BaseActivity.ToolbarLeftImageClickListener {
                override fun onLeftImageClicked() {
                    (activity as MainActivity).onBackPressed()
                }
            })


        binding.clWriteView.setOnClickListener {
            isEdit = false
            binding.clWriteView.visibility = View.INVISIBLE
            binding.clPostView.visibility = View.GONE

            showToolbarRightIcon()
        }
    }

    /**
     * Get Add community opinion Response
     */
    @SuppressLint("NewApi")
    private val addCommunityOpinionObserver = Observer<BaseResponse> {
        if (it.status) {
            seletedImageArr.clear()
            binding.clWriteView.visibility = View.INVISIBLE
            binding.clPostView.visibility = View.GONE

            showToolbarRightIcon()

            AppUtils.showSnackBar(binding.root, it.message)
            mViewModel.callGetGoddessCommunityOpinions()
        } else {
            AppUtils.showSnackBar(binding.root, it.message)
        }
    }

    /**
     * Get Opinion List Response observer
     */
    private val opinionsObserver = Observer<CommunityOpinionResponse> {
        if (it.status) {

            if (CURRENT_PAGE == 1) {
                TOTAL_PAGE = it.pagination.total

                if (it.result.isNotEmpty()) {
                    deleteMedia = ""
                    seletedImageArr.clear()
                    binding.clWriteView.visibility = View.INVISIBLE
                    binding.clPostView.visibility = View.GONE

                    showToolbarRightIcon()

                    binding.tvPostDesc.setText("")
                    imageAdapter.clear()
                    binding.rbAnonymous.isChecked = true
                    binding.rbYourSelf.isChecked = false
                    adapter.setItem(it.result as ArrayList<CommunityOpinionResult?>)
                } else {
                    setNoOpinionData()
                }
            } else {
                (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).removeLoadingFooter(
                    true
                )
                val adapterCount =
                    (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).itemCount
                (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).removeItem(
                    adapterCount - 1
                )

                val sizeList =
                    (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).itemCount
                (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).addItems(it.result as ArrayList<CommunityOpinionResult?>)
                (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).notifyItemRangeInserted(
                    sizeList,
                    it.result.size
                )
            }


        } else {
            if (CURRENT_PAGE == 1) {
                setNoOpinionData()
            } else {
                AppUtils.showSnackBar(binding.root, it.message)
            }
        }
    }

    /**
     * Get Opinion Like or Unlike  Response observer
     */

    private val opinionsLikeUnlikeObserver = Observer<LikeOpinionResponse> {
        if (it.status!!) {

            val result = it.result
            result?.let {
                selectedData.likes_count = it.likesCount!!.toString()
                selectedData.already_liked = it.alreadyLiked!!
            }

            /*if (selectedData.already_liked) {
                selectedData.already_liked = false
                var count: Int = selectedData.likes_count.toInt() - 1
                selectedData.likes_count = count.toString()
            } else {
                selectedData.already_liked = true
                var count: Int = selectedData.likes_count.toInt() + 1
                selectedData.likes_count = count.toString()
            }*/

            adapter.updateItem(selectedDataPosition, selectedData)

            //mViewModel.callGetGoddessCommunityOpinions(CURRENT_PAGE, showProgress=false)
            //adapter.notifyItemChanged(selectedDataPosition, selectedData)
        }
    }

    /**
     * Delete opinion observer
     */
    private val deleteOpinionobserver = Observer<BaseResponse> {
        mViewModel.callGetGoddessCommunityOpinions()
    }

    /**
     * Video compress response
     */

    private val videoCompressResponse = Observer<String> {
        if (it.isNotEmpty()) {
            AppUtils.logE(it)
            setImageList(it)
            val file = File(it)
            AppUtils.logE("" + file.length() / (1024 * 1024))

        } else {
            AppUtils.showToast(context!!, "Please try again")
        }
    }

    /**
     * Set Opinion Adapter
     */
    private fun setAdapter() {
        binding.rvCommunity.layoutManager = LinearLayoutManager(context!!)
        adapter = GoddessCommunityOpinionsAdapter()
        binding.rvCommunity.setHasFixedSize(true)
        binding.rvCommunity.adapter = adapter
        adapter.itemClickListener = this
    }

    /**
     * Set Add opinion Image adapter
     */
    private fun setOpinionImageAdapter() {
        context.let {
            imageAdapter = OpinionImageAdapter(this)
            binding.rvImages.setHasFixedSize(true)
            binding.rvImages.adapter = imageAdapter
        }
    }


    private fun setNoOpinionData() {
        binding.clNoOpinion.visibility = View.VISIBLE
        binding.clOpinionDataView.visibility = View.GONE
    }

    override fun getViewModel(): GoddessCommunityOpinionsViewModel {
        mViewModel = ViewModelProvider(this).get(GoddessCommunityOpinionsViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        AppUtils.logE("" + requestCode)
        if (requestCode == AppConstants.OPEN_COMMENT && resultCode == Activity.RESULT_OK) {
            mViewModel.callGetGoddessCommunityOpinions()
        } else if (requestCode == PICK_VIDEO_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            intent?.let {
                val selectedImage: Uri = it.data!!
                val filePath = arrayOf(MediaStore.Video.Media.DATA)
                context?.let {
                    val c: Cursor? =
                        it.contentResolver.query(selectedImage, filePath, null, null, null)
                    c?.let {
                        c.moveToFirst()
                        val columnIndex = c.getColumnIndex(filePath[0])
                        val videoPath = c.getString(columnIndex)
                        c.close()
                        AppUtils.logE(videoPath)
                        val file = File(videoPath)
                        AppUtils.logE("" + file.length() / (1024 * 1024))
                        val size: Int = (file.length() / (1024 * 1024)).toInt()
                        if (size > VIDEO_SIZE) {
                            mViewModel.compressVideo(
                                activity!!.externalCacheDir!!.absolutePath,
                                captureVideoFileName.path!!
                            )
                        } else {
                            setImageList(videoPath)
                        }

                    }
                }
            }
        } else if (requestCode == PICK_VIDEO_CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            intent.let {
                val file = File(captureVideoFileName.path!!)
                //Log.e("VIDEO_PATH", ""+captureVideoFileName.path!!);
                //val file = File(FileUtils.getPath(context!!, captureVideoFileName))
                //Log.e("VIDEO_PATH", ""+captureVideoFileName.path!!);

                AppUtils.logE("" + file.length().toInt() / 1024)
                if (file.length().toInt() != 0) {
                    AppUtils.logE("" + file.length() / (1024 * 1024))
                    val size: Int = (file.length() / (1024 * 1024)).toInt()
                    if (size > VIDEO_SIZE) {
                        mViewModel.compressVideo(
                            activity!!.externalCacheDir!!.absolutePath,
                            captureVideoFileName.path!!
                        )
                    } else {
                        setImageList(captureVideoFileName.path!!)
                    }
                }
            }
        } else {
            filePickUtils?.onActivityResult(requestCode, resultCode, intent)
        }
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

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.tvSelectCamera -> {
                    if (isSelecteVideo) {
                        if (checkAndRequestPermissions()) {
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            captureVideoFileName = getOutputMediaFileUri(MEDIA_TYPE_VIDEO)
                            // set the image file name
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, captureVideoFileName)

                            // set the video image quality to high
                            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                            startActivityForResult(intent, PICK_VIDEO_CAMERA_REQUEST_CODE)
                        }
                    } else {
                        filePickUtils?.requestImageCamera(
                            PICK_IMAGE_CAMERA_REQUEST_CODE,
                            allowCrop = false,
                            isFixedRatio = false
                        )
                    }

                    mImageSelectionDialog.cancel()
                }
                R.id.tvSelectGallery -> {
                    if (isSelecteVideo) {
                        if (checkAndRequestPermissions()) {
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            )
                            intent.type = "video/mp4"
                            startActivityForResult(intent, PICK_VIDEO_GALLERY_REQUEST_CODE)
                        }
                    } else {
                        filePickUtils?.requestImageGallery(
                            PICK_IMAGE_GALLERY_REQUEST_CODE,
                            allowCrop = false,
                            isFixedRatio = false
                        )
                    }
                    mImageSelectionDialog.cancel()
                }
            }
        }
    }

    /**
     * Community recyclerview item click
     */
    override fun onItemClick(view: View, data: CommunityOpinionResult?, position: Int) {

        if (data == null) {
            return
        }

        when (view.id) {
            R.id.tvComment -> {
                if (prefs.isLoggedIn) {
                    context?.let {
                        startActivityForResult(
                            ActivityComments.newInstance(it, data.gco_id, data.comments_count),
                            AppConstants.OPEN_COMMENT
                        )
                    }
                } else {
                    context?.let {
                        startActivity(LoginActivity.newInstance(it, ""))
                    }
                }

            }
            R.id.tvLike -> {
                if (prefs.isLoggedIn) {
                    selectedDataPosition = position
                    selectedData = data
                    mViewModel.callLikeUnlikeOpinion(data.gco_id)
                } else {
                    context?.let {
                        startActivity(LoginActivity.newInstance(it, ""))
                    }
                }
            }

            R.id.ivOption -> {
                if (prefs.isLoggedIn) {
                    prefs.userDataModel?.result?.let {
                        if (data.added_by != null && data.added_by.u_id == it.uId) {
                            showOptionMenu(view, data)
                        } else {
                            // AppUtils.showToast(context!!, "Your are not delete this opinion.")
                            AppUtils.showSnackBar(
                                view,
                                "You are not allowed to edit OR Delete this opinion"
                            )
                        }
                    }

                } else {
                    context?.let {
                        startActivity(LoginActivity.newInstance(it, ""))
                    }

                }
            }
        }
    }

    /**
     * Open option menu
     */

    private fun showOptionMenu(view: View, data: CommunityOpinionResult) {
        val popupMenu = PopupMenu(context!!, view)
        popupMenu.inflate(R.menu.opinion_menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.edit -> {
                    if (data.media.isNotEmpty()) {
                        for (media in data.media) {
                            val data = ImageData(media.gcom_id.toString(), media.gcom_media!!)
                            seletedImageArr.add(data)
                        }
                    }
                    binding.clWriteView.visibility = View.VISIBLE
                    binding.clPostView.visibility = View.VISIBLE

                    hideToolbarRightIcon()

                    binding.tvPostDesc.setText(data.gco_text)
                    if (data.gco_show_identity == 1) {
                        binding.rbYourSelf.isChecked = true
                    } else {
                        binding.rbAnonymous.isChecked = true
                    }
                    if (seletedImageArr.isNotEmpty()) {
                        imageAdapter.setItem(seletedImageArr)
                    }
                    isEdit = true
                    editId = data.gco_id
                    binding.tvTextCount.text = "" + data.gco_text.length + "/500"
                }
                R.id.delete -> {
                    mViewModel.deleteOpinion(data.gco_id)
                }
            }

            true
        })
        popupMenu.show()
    }

    /**
     * Selected image recyclerview Click
     */
    override fun onImageRemove(data: ImageData, position: Int) {
        setImageList(data.imageName, true, position)
        if (data.imageId.isNotEmpty()) {
            if (deleteMedia.isEmpty()) {
                deleteMedia = data.imageId
            } else {
                deleteMedia = deleteMedia + "," + data.imageId
            }
            AppUtils.logE(deleteMedia)
        }
    }

    /**
     * Show Image selection Dialog
     */
    fun showImageSelectionDialog(isSelecteVideo: Boolean = false) {
        if (seletedImageArr.isNotEmpty() && seletedImageArr.size == 5) {
            context?.let {
                AppUtils.showToast(it, getString(R.string.select_five))
            }
        } else {
            this.isSelecteVideo = isSelecteVideo
            mImageSelectionDialog = BottomSheetDialog(context!!)
            val dialogImageSelectionBinding = DialogImageSelectionBinding.inflate(layoutInflater)
            mImageSelectionDialog.setContentView(dialogImageSelectionBinding.root)
            dialogImageSelectionBinding.tvSelectCamera.setOnClickListener(this)
            dialogImageSelectionBinding.tvSelectGallery.setOnClickListener(this)
            mImageSelectionDialog.show()
        }

    }

    /**
     * Add or Remove image in selected list
     */
    private fun setImageList(imagePath: String = "", isRemove: Boolean = false, position: Int = 0) {
        if (isRemove) {
            // seletedImageList.removeAt(position)
            seletedImageArr.removeAt(position)
        } else {
            //seletedImageList.add(imagePath)
            val imageData = ImageData("", imagePath)
            seletedImageArr.add(imageData)
        }

        if (seletedImageArr.isNotEmpty()) {
            imageAdapter.setItem(seletedImageArr)
        } else {
            imageAdapter.notifyDataSetChanged()
        }
    }

    /* Add community Opinion */
    fun addCommunityOpinion() {
        var postAsa = ""
        if (binding.rbYourSelf.isChecked) {
            postAsa = "1"
        }
        if (binding.rbAnonymous.isChecked) {
            postAsa = "2"
        }
        context?.let {
            mViewModel.checkAddOpinionValidation(
                it,
                binding.tvPostDesc.text.toString(),
                postAsa,
                seletedImageArr, isEdit, deleteMedia, editId
            )

        }

    }


    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
        //return FileProvider.getUriForFile(activity!!, context!!.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(type)!!)
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        val fullPath =
            activity!!.externalCacheDir!!.absolutePath + "/Goddess"
        // Check that the SDCard is mounted
        val mediaStorageDir = File(fullPath)
        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.")
                return null
            }
        }
        // Create a media file name

        // For unique file name appending current timeStamp with file name
        val date = java.util.Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(date.time)
        val mediaFile: File
        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = File(
                (mediaStorageDir.path + File.separator +
                        "VID_" + timeStamp + ".mp4")
            )
            var videoFileName = mediaFile.name
        } else {
            return null
        }
        return mediaFile
    }

    private fun checkAndRequestPermissions(): Boolean {
        context?.let {
            val camerapermission = ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            val writepermission =
                ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)


            val listPermissionsNeeded = ArrayList<String>()

            if (camerapermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA)
            }
            if (writepermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    listPermissionsNeeded.toTypedArray(),
                    10101
                )
                return false
            }
        }
        return true
    }

    /**
     * Opinion content text watcher
     */
    private fun setTextCount() {
        binding.tvPostDesc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            @SuppressLint("SetTextI18n")
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.tvTextCount.text = "0/500"
                } else {
                    s.let {
                        binding.tvTextCount.text = "" + it.length + "/500"
                    }
                }
            }
        })
    }

    var CURRENT_PAGE: Int = 1
    var isLoading: Boolean = true
    var TOTAL_PAGE: Int = 1

    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvCommunity.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvCommunity.layoutManager as LinearLayoutManager,
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
                        (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).addItem(
                            null
                        )
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).itemCount
                        (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvCommunity.adapter as GoddessCommunityOpinionsAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        mViewModel.callGetGoddessCommunityOpinions(CURRENT_PAGE, false)
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