package com.krystal.goddesslifestyle.viewmodels

import android.app.Application
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.krystal.goddesslifestyle.GoddessLifeStyleApp
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseResponse
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.data.model.ImageData
import com.krystal.goddesslifestyle.data.response.CommunityOpinionResponse
import com.krystal.goddesslifestyle.data.response.LikeOpinionResponse
import com.krystal.goddesslifestyle.utils.ApiParam
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.ImagesConvertZipFile
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.net.SocketTimeoutException


/**
 * Created by imobdev on 30/3/20
 */
class GoddessCommunityOpinionsViewModel(application: Application) : BaseViewModel(application) {
    /*RxJava Subscription object for api calling*/
    private var subscription: Disposable? = null
    private var zipFolderpath = ""
    private var videoZipFolderpath = ""
    private var imageFolderPath = ""
    private var videoFolderPath = ""

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    private val opinionsResponse: MutableLiveData<CommunityOpinionResponse> by lazy {
        MutableLiveData<CommunityOpinionResponse>()
    }

    fun getCommunityOpinionsResponse(): LiveData<CommunityOpinionResponse> {
        return opinionsResponse
    }

    private val opinionsLikeUnlikeResponse: MutableLiveData<LikeOpinionResponse> by lazy {
        MutableLiveData<LikeOpinionResponse>()
    }

    fun getCommunityOpinionsLikeUnlikeResponse(): LiveData<LikeOpinionResponse> {
        return opinionsLikeUnlikeResponse
    }

    private val addOpinionResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getAddOpinionResponseResponse(): LiveData<BaseResponse> {
        return addOpinionResponse
    }

    private val deleteOpinionsResponse: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getDeleteOpinionResponse(): LiveData<BaseResponse> {
        return deleteOpinionsResponse
    }

    private val compressVideoResponse: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getCompressVideoResponse(): LiveData<String> {
        return compressVideoResponse
    }

    fun callGetGoddessCommunityOpinions(pageNo: Int = 1, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, String> = HashMap()
            params[ApiParam.KEY_PAGE] = pageNo.toString()
            subscription = apiServiceObj
                .apiGetGoddessCommunityOpinions(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleOpinionResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleOpinionResponse(response: CommunityOpinionResponse) {
        opinionsResponse.value = response
    }


    fun callLikeUnlikeOpinion(opinionId: Int, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.ID] = opinionId

            subscription = apiServiceObj
                .apiLikeUnlikeCommunityOpinioun(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleOpinionLikeUnlikeResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleOpinionLikeUnlikeResponse(response: LikeOpinionResponse) {
        opinionsLikeUnlikeResponse.value = response
    }


    fun checkAddOpinionValidation(
        context: Context, opinionText: String,
        postAsa: String,
        seletedImageList: ArrayList<ImageData>, isEdit: Boolean, deleteMedia: String, id: Int
    ) {
        var isValid = true
        if (opinionText.isEmpty()) {
            apiErrorMessage.value = "Please enter opinion."
            isValid = false
            return
        }
        if (postAsa.isEmpty()) {
            apiErrorMessage.value = context.getString(R.string.select_post_as_a)
            isValid = false
            return
        }

        if (isValid) {
            var imageList = arrayListOf<String>()
            var videoList = arrayListOf<String>()

            if (seletedImageList.isNotEmpty()) {
                for (item in seletedImageList) {
                    if (item.imageId.isEmpty()) {
                        if (item.imageName.endsWith(".mp4")) {
                            videoList.add(item.imageName)
                        } else {
                            imageList.add(item.imageName)
                        }
                    }
                }
            }
            if (isEdit) {
                callEditOpinionAPI(
                    opinionText,
                    postAsa,
                    imageList, videoList, deleteMedia, id
                )
            } else {
                callAddOpinionAPI(
                    opinionText,
                    postAsa,
                    imageList, videoList
                )
            }

        }
    }

    private fun callAddOpinionAPI(
        opinionText: String,
        postAsA: String,
        imageList: ArrayList<String>, videoList: ArrayList<String>,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String, String> = HashMap()
        var imagePath = ""
        var videoPath = ""
        params[ApiParam.KEY_OPINION_TEXT] = opinionText
        params[ApiParam.KEY_OPINION_POST_AS_A] = postAsA



        if (imageList.isNotEmpty()) {
            /* Convert All images in ZIP folder */
            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()


            /*val direct = File(AppConstants.Images_PATH + "images")*/
            val direct =
                File(getApplication<GoddessLifeStyleApp>().filesDir.absolutePath + File.separator + "images")


            if (!direct.exists()) {
                //if (direct.mkdir()); //directory is created;
                direct.mkdir()
            }
            ImagesConvertZipFile.copyOrMoveFile(direct, imageList)
            ImagesConvertZipFile.zipFileAtPath(direct.toString(), "$direct.zip")
            imagePath = "$direct.zip"
            zipFolderpath = imagePath
            imageFolderPath = direct.toString()
            AppUtils.logE(zipFolderpath)
            AppUtils.logE(imageFolderPath)
        }

        if (videoList.isNotEmpty()) {
            //val direct = File(AppConstants.Images_PATH + "videos")
            val direct =
                File(getApplication<GoddessLifeStyleApp>().filesDir.absolutePath + File.separator + "videos")
            if (!direct.exists()) {
                if (direct.mkdir()); //directory is created;
            }
            if (!direct.exists()) {
                if (direct.mkdir()); //directory is created;
            }
            ImagesConvertZipFile.copyOrMoveFile(direct, videoList)
            ImagesConvertZipFile.zipFileAtPath(direct.toString(), "$direct.zip")
            videoPath = "$direct.zip"

            videoFolderPath = direct.toString()
            videoZipFolderpath = videoPath
            AppUtils.logE(videoZipFolderpath)
            AppUtils.logE(videoFolderPath)
        }
        val parameters = getParamsRequestBody(params, imagePath, videoPath)
        subscription = apiServiceObj
            .apiAddCommunityOpinion(parameters)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { if (showProgress) onApiStart() }
            .doOnTerminate { if (showProgress) onApiFinish() }
            .subscribe(this::handleAddOpinionResponse, this::handleError)
    }


    private fun callEditOpinionAPI(
        opinionText: String,
        postAsA: String,
        imageList: ArrayList<String>, videoList: ArrayList<String>, deleteMedia: String, id: Int,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String, String> = HashMap()
        var imagePath = ""
        var videoPath = ""
        params[ApiParam.ID] = id.toString()
        params[ApiParam.KEY_OPINION_TEXT] = opinionText
        params[ApiParam.KEY_OPINION_POST_AS_A] = postAsA
        if (deleteMedia.isNotEmpty()) {
            params[ApiParam.KEY_DELETE_MEDIA] = deleteMedia
        }

        if (imageList.isNotEmpty()) {
            /* Convert All images in ZIP folder */
            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()


            //val direct = File(AppConstants.Images_PATH + "images")
            val direct =
                File(getApplication<GoddessLifeStyleApp>().filesDir.absolutePath + File.separator + "images")


            if (!direct.exists()) {
                if (direct.mkdir()); //directory is created;
            }
            ImagesConvertZipFile.copyOrMoveFile(direct, imageList)
            ImagesConvertZipFile.zipFileAtPath(direct.toString(), "$direct.zip")
            imagePath = "$direct.zip"
            zipFolderpath = imagePath
            imageFolderPath = direct.toString()
            AppUtils.logE(zipFolderpath)
            AppUtils.logE(imageFolderPath)
        }

        if (videoList.isNotEmpty()) {

            //val direct = File(AppConstants.Images_PATH + "videos")
            val direct =
                File(getApplication<GoddessLifeStyleApp>().filesDir.absolutePath + File.separator + "videos")


            if (!direct.exists()) {
                if (direct.mkdir()); //directory is created;
            }
            if (!direct.exists()) {
                if (direct.mkdir()); //directory is created;
            }
            ImagesConvertZipFile.copyOrMoveFile(direct, videoList)
            ImagesConvertZipFile.zipFileAtPath(direct.toString(), "$direct.zip")
            videoPath = "$direct.zip"

            videoFolderPath = direct.toString()
            videoZipFolderpath = videoPath
            AppUtils.logE(videoZipFolderpath)
            AppUtils.logE(videoFolderPath)
        }
        val parameters = getParamsRequestBody(params, imagePath, videoPath)
        subscription = apiServiceObj
            .apiEditCommunityOpinion(parameters)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { if (showProgress) onApiStart() }
            .doOnTerminate { if (showProgress) onApiFinish() }
            .subscribe(this::handleAddOpinionResponse, this::handleError)
    }


    private fun handleAddOpinionResponse(response: BaseResponse) {
        zipFolderpath.let { AppUtils.deleteFolder(it) }
        imageFolderPath.let { AppUtils.deleteFolder(it) }
        videoFolderPath.let { AppUtils.deleteFolder(it) }
        videoZipFolderpath.let { AppUtils.deleteFolder(it) }
        addOpinionResponse.value = response
    }

    /* Convert param in multipart request body*/
    private fun getParamsRequestBody(
        params: HashMap<String, String>,
        imagePath: String, videoPath: String
    ): HashMap<String, RequestBody> {
        val resultParams = HashMap<String, RequestBody>()

        for ((key, value) in params) {
            val body = RequestBody.create("text/plain".toMediaTypeOrNull(), value)
            resultParams.put(key, body)
        }

        if (!TextUtils.isEmpty(imagePath)) {
            val file = File(imagePath)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val imageParams = ApiParam.KEY_OPINION_IMAGES + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }

        if (!TextUtils.isEmpty(videoPath)) {
            val file = File(videoPath)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val videoParams = ApiParam.KEY_OPINION_VIDEO + "\";filename=\"${file.name}\""
            resultParams[videoParams] = reqFile
        }
        return resultParams
    }


    fun deleteOpinion(opinionId: Int, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val params: HashMap<String, Any> = HashMap()
            params[ApiParam.ID] = opinionId

            subscription = apiServiceObj
                .apiDeleteOpinion(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleDeleteOpinionResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleDeleteOpinionResponse(response: BaseResponse) {
        if (response.status) {
            deleteOpinionsResponse.value = response
            apiErrorMessage.value = response.message
        } else {
            apiErrorMessage.value = response.message
        }
    }

    private fun handleError(error: Throwable) {
        onApiFinish()
        if (error is SocketTimeoutException) {
            AppUtils.showToast(
                getApplication(),
                getApplication<GoddessLifeStyleApp>().getString(R.string.connection_timed_out)
            )
        }
    }

    fun compressVideo(folderPath: String, file: String) {
        loadingVisibility.value = true
        val outputFile =
            folderPath + File.separator + System.currentTimeMillis() + ".mp4"
        /* GiraffeCompressor.create() //two implementations: mediacodec and ffmpeg,default is mediacodec
             .input(file) //set video to be compressed
             .output(outputFile) //set compressed video output
             .bitRate(200)
             .resizeFactor(0.8F)
             .watermark(file)
             .ready()
             .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
             .subscribe(object: Subscriber<GiraffeCompressor.Result>() {
                 override fun onNext(re: GiraffeCompressor.Result?) {
                     Log.e("OUT",""+re!!.output)
                     Log.e("INPUT",""+file)

                     compressVideoResponse.value=re.output
                     loadingVisibility.value=false
                 }

                 override fun onCompleted() {
                     Log.e("COM","onCompleted")
                     loadingVisibility.value=false

                     // AppUtils.showToast(getApplication(),""+(output.length/1024)+ " KB");


                 }

                 override fun onError(e: Throwable?) {
                     loadingVisibility.value=false
                     compressVideoResponse.value=""
                 }

             })*/7889178080

        var time = 0L
        /*VideoCompressor.doVideoCompression(
            file,
            outputFile,
            object : CompressionListener {
                override fun onProgress(percent: Float) {
                    //Update UI
                }
                override fun onStart() {
                    time = System.currentTimeMillis()
                }
                override fun onSuccess() {
                    compressVideoResponse.value=outputFile
                    loadingVisibility.value=false
                }
                override fun onFailure() {

                }
            })*/
    }
}