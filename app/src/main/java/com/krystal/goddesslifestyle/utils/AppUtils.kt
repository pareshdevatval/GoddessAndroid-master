package com.krystal.goddesslifestyle.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import com.bumptech.glide.Glide
import com.georeminder.utils.filePick.FileUri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.krystal.goddesslifestyle.BuildConfig
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.model.Benefit
import com.krystal.goddesslifestyle.data.response.AppSettingsResponse
import com.krystal.goddesslifestyle.data.response.UserSubscription
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.subscription.SubscriptionActivity
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and

/**
 * Created by Bhargav Thanki on 19/12/18.
 */
object AppUtils {

    /**
     * A method which returns the state of internet connectivity of user's phone.
     */


    fun hasInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun logE(msg: String, title: String = "GODDES_LIFESTYLE") {
        Log.e(title, msg)
    }

    /**
     * A common method used in whole application to show a snack bar
     */
    fun showSnackBar(v: View, msg: String) {
        val mSnackBar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
        val view = mSnackBar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        //  params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(ContextCompat.getColor(v.context, R.color.pink))
        val mainTextView =
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        mainTextView.setTextColor(Color.WHITE)
        //mainTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.context.resources.getDimension(R.dimen.medium))
        mainTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        mainTextView.maxLines = 4
        mainTextView.gravity = Gravity.CENTER_HORIZONTAL
        mSnackBar.show()
    }

    /**
     * A common method to logout the user and redirect to login screen
     */
    fun logoutUser(
        context: Context?,
        prefs: Prefs?
    ) {
        context?.let {
            Prefs.getInstance(it)?.userDataModel = null
            prefs?.let {
                val rememberMe = it.rememberMe
                if (rememberMe) {
                    val currentUsername = it.userName
                    val currentPassword = it.password
                    val stripePublicKey = it.stripePublicKey
                    val stripeSecretKey = it.stripeSecretKey
                    val settings = it.settings
                    it.clearPrefs()
                    it.rememberMe = true
                    it.userName = currentUsername
                    it.password = currentPassword
                    it.stripePublicKey = stripePublicKey
                    it.stripeSecretKey = stripeSecretKey
                    it.settings = settings
                } else {
                    val stripePublicKey = it.stripePublicKey
                    val stripeSecretKey = it.stripeSecretKey
                    val setting = it.settings
                    it.clearPrefs()
                    it.stripePublicKey = stripePublicKey
                    it.stripeSecretKey = stripeSecretKey
                    it.settings = setting
                }
            }
            val i = Intent(context, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.startActivity(i)
        }
    }

    fun logoutWithDialog(context: Context?) {
        context?.let {
            val prefs = Prefs.getInstance(it)

            val builder = AlertDialog.Builder(it)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")

            builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()
                    prefs?.let {
                        val rememberMe = it.rememberMe
                        if (rememberMe) {
                            val currentUsername = it.userName
                            val currentPassword = it.password
                            it.clearPrefs()
                            it.rememberMe = true
                            it.userName = currentUsername
                            it.password = currentPassword
                        } else {
                            it.clearPrefs()
                        }
                    }
                    val i = Intent(context, LoginActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    it.startActivity(i)
                    //AppUtils.startFromRightToLeft(this)
                }
            })

            builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()
                }
            })

            val dialog = builder.create()
            dialog.show()

        }
    }

    fun showSimpleDialog(context: Context, message: String) {
        val builder = AlertDialog.Builder(context)
        //builder.setTitle("")
        builder.setMessage(message)

        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.dismiss()
            }
        })

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * A method to convert the password in SHA1
     */
    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun SHA1(text: String?): String {
        val md = MessageDigest.getInstance("SHA-1")
        if (text != null) {
            md.update(text.toByteArray(charset("iso-8859-1")), 0, text.length)
        }
        val sha1hash = md.digest()
        return convertToHex(sha1hash)
    }

    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfbyte = (b.toInt() ushr 4) and 0x0F
            var two_halfs = 0
            do {
                if (0 <= halfbyte && halfbyte <= 9)
                    buf.append(('0'.toInt() + halfbyte).toChar())
                else
                    buf.append(('a'.toInt() + (halfbyte - 10)).toChar())
                halfbyte = (b and 0x0F).toInt()
            } while (two_halfs++ < 1)
        }
        return buf.toString()
    }


    fun formatDate(year: Int, month: Int, day: Int, opFormat: String): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 0
        cal.set(year, month, day)
        val date = cal.time
        val sdf = SimpleDateFormat(opFormat, Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDate(date: Date, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(opFormat, Locale.getDefault()).format(date)
    }

    @JvmStatic
    fun formatDate(date: String?, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(opFormat, Locale.getDefault()).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    @JvmStatic
    fun formatDate(date: String?): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(
                AppConstants.DEFAULT_DATE_FORMAT,
                Locale.getDefault()
            ).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    @JvmStatic
    fun getValue(value: String?) = if (value == null) "" else value

    @JvmStatic
    fun getValue(value: Int?) = if (value == null) 1 else value


    fun formatDate(date: Long): String {
        return formatDate(Date(date), AppConstants.DEFAULT_DATE_FORMAT)
    }

    /**
     * A method to get the external directory of phone, to store the image file
     */
    fun getWorkingDirectory(activity: Activity?): File {
        val directory = File(activity!!.externalCacheDir, "." + BuildConfig.APPLICATION_ID)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return directory
    }

    /**
     * This method is for making new jpg file.
     * @param activity Instance of activity
     * @param prefix file name prefix
     */
    fun createImageFile(activity: Activity, prefix: String): FileUri? {
        val fileUri = FileUri()

        var image: File? = null
        try {
            image = File.createTempFile(
                prefix + System.currentTimeMillis().toString(),
                ".jpg",
                getWorkingDirectory(activity)
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (image != null) {
            fileUri.file = image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri.imageUrl =
                    (FileProvider.getUriForFile(
                        activity,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        image
                    ))
            } else {
                fileUri.imageUrl = (Uri.parse("file:" + image.absolutePath))
            }
        }
        return fileUri
    }

    /**
     * A method to show device keyboard for user input
     */
    fun showKeyboard(activity: Activity?, view: EditText) {
        try {
            val inputManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            Log.e("Exception showKeyboard", e.toString())
        }
    }

    /**
     * A method to hide the device's keyboard
     */
    fun hideKeyboard(activity: Activity) {
        if (activity.currentFocus != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /*
* get mobile screen height and width
* */
    fun getAnimationSize(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        var height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        if (height > 1200) {
            height -= 650
        } else {
            height -= 450
        }
        Log.e("height", "" + height)
        return height
    }

    fun loadImageThroughGlide(
        context: Context,
        imageView: ImageView,
        url: String?,
        placeHolderResource: Int
    ) {
        if (url.isNullOrBlank()) {
            //imageView.setImageResource(placeHolderResource)
            //imageView.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(context)
                .load(placeHolderResource)
                .fitCenter()
                .into(imageView)
        } else {
            //imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(context)
                .load(url)
                .placeholder(placeHolderResource)
                //.fitCenter()
                .error(placeHolderResource)
                .into(imageView)
        }
    }

    fun loadImageThroughGlide(
        context: Context,
        imageView: ImageView,
        url: String?,
        placeHolderResource: Drawable
    ) {
        if (url.isNullOrBlank()) {
            //imageView.setImageResource(placeHolderResource)
            //imageView.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(context)
                .load(placeHolderResource)
                .fitCenter()
                .into(imageView)
        } else {
            //imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(context)
                .load(url)
                .placeholder(placeHolderResource)
                //.fitCenter()
                .error(placeHolderResource)
                .into(imageView)
        }
    }

    fun loadImages(
        context: Context,
        url: String,
        imageView: AppCompatImageView,
        placeHolderResource: Int
    ) {
        Glide.with(context)
            .load(BuildConfig.IMAGE_BASE_URL + url)
            .placeholder(placeHolderResource)
            .error(placeHolderResource)
            .into(imageView)
    }

    /**
     * Terms and condition or Privacy Policy spannable and click
     */
    fun setClickableString(
        context: Context,
        wholeValue: String,
        textView: androidx.appcompat.widget.AppCompatTextView,
        clickableValue: Array<String>,
        clickableSpans: Array<ClickableSpan>
    ) {
        val spannableString = SpannableString(wholeValue)

        for (i in clickableValue.indices) {
            val clickableSpan = clickableSpans[i]
            val link = clickableValue[i]

            val startIndexOfLink = wholeValue.indexOf(link)

            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), wholeValue.indexOf(link),
                wholeValue.indexOf(link) + link.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )


            spannableString.setSpan(
                clickableSpan,
                startIndexOfLink,
                startIndexOfLink + link.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                UnderlineSpan(),
                wholeValue.indexOf(link),
                wholeValue.indexOf(link) + link.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )


        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(spannableString, TextView.BufferType.SPANNABLE)

    }

    fun generateImageUrl(imageName: String?, width: Int): String {
        if (imageName.isNullOrBlank()) {
            return ""
        }
        return BuildConfig.IMAGE_BASE_URL + imageName +
                AppConstants.KEY_WIDTH + width +
                AppConstants.KEY_QUALITY + "100" +
                AppConstants.KEY_SCALE_TYPE + AppConstants.SCALE_TYPE_CONTAIN
    }

    fun generateImageUrl(
        imageName: String?, width: Int = 0, height: Int = 0,
        scaleType: String = AppConstants.SCALE_TYPE_CROP
    ): String {
        if (imageName.isNullOrBlank()) {
            return ""
        }
        return BuildConfig.IMAGE_BASE_URL + imageName +
                AppConstants.KEY_WIDTH + width +
                AppConstants.KEY_HEIGHT + height +
                AppConstants.KEY_QUALITY + "100" +
                AppConstants.KEY_SCALE_TYPE + scaleType
    }

    fun startFromRightToLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.trans_left_in,
            R.anim.trans_left_out
        )
    }

    fun getScreenHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun printHashKey(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e("GODDESS---->", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("GODDESS---->", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("GODDESS---->", "printHashKey()", e)
        }

    }

    fun shareContent(context: Context, shareText: String) {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    fun startSubscriptionActivity(context: Context?, disableBasicPlan: Boolean = false) {
        context?.let {
            if (context is Activity) {
                (it as Activity).startActivityForResult(
                    SubscriptionActivity.newInstance(it, disableBasicPlan),
                    AppConstants.REQUEST_CODE_SUBSCRIPTION_SCREEN
                )
                startFromRightToLeft(it)
            }
        }
    }

    /* Wave Animation */
    @SuppressLint("NewApi")
    fun startWaveAnimation(context: Context?, waveView: WaveView, color: Int) {
        if (context == null) {
            return
        }

        val pixels = AppUtilsJava.getPixelsFromDp(context!!, 20f).toInt()

        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 10).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (1000 + Math.random() * 600).toLong(),
                true
            )
        )
        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 10).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                true
            )
        )

        /* Right to Left */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 10).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.5f,
                (2000 + Math.random() * 1000).toLong(),
                false
            )
        )
        waveView.startAnimation()
    }

    /* Wave Animation */
    @SuppressLint("NewApi")
    fun startNotificationWaveAnimation(context: Context?, waveView: WaveView, color: Int) {
        if (context == null) {
            return
        }
        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((AppUtils.getAnimationSize(context) - 100) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.8f,
                (1000 + Math.random() * 600).toLong(),
                true
            )
        )
        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((AppUtils.getAnimationSize(context) - 100) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.6f,
                (2000 + Math.random() * 1000).toLong(),
                true
            )
        )

        /* Right to Left */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((AppUtils.getAnimationSize(context) - 100) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.8f,
                (2000 + Math.random() * 1000).toLong(),
                false
            )
        )
        waveView.startAnimation()
    }


    fun finishActivity(activity: Activity?) {
        activity?.let {
            val returnIntent = Intent()
            it.setResult(Activity.RESULT_OK, returnIntent)
            it.finish()

            GoddessAnimations.finishFromLeftToRight(it)
        }

    }

    // common method to get price with currency
    // so if currency gets changed then need to change at this one place only
    fun getPriceWithCurrency(context: Context?, price: String?): String {
        if (price == null) {
            return ""
        } else {
            context?.let {
                val currency = it.getString(R.string.currency)
                return "$currency $price"
            } ?: return price
        }
    }


    /* Wave Animation */
    @SuppressLint("NewApi")
    fun setWaveAnimation(context: Context, color: Int, waveView: WaveView) {
        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((getAnimationSize(context) - 150) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.7f,
                (1000 + Math.random() * 600).toLong(),
                true
            )
        )
        /* Left to Right */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((getAnimationSize(context) - 150) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.7f,
                (2000 + Math.random() * 1000).toLong(),
                true
            )
        )

        /* Right to Left */
        waveView.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                ((getAnimationSize(context) - 150) + Math.random() * 20).toFloat(),
                (Math.random() * 50).toFloat(),
                ContextCompat.getColor(context!!, color),
                ContextCompat.getColor(context!!, color),
                /*context!!.getColor(color),
                context!!.getColor(color),*/
                0.7f,
                (2000 + Math.random() * 1000).toLong(),
                false
            )
        )
        waveView.startAnimation()
    }

    @SuppressLint("SimpleDateFormat")
    fun countDays(dateStr: String): String {
        var sDate = convertGMTT_Local(dateStr)
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate)

        //  val milliseconds = date.getTime()
        //val minutes = ((milliseconds / (1000 * 60)) % 60).toInt()
        //val hours = ((milliseconds / (1000 * 60 * 60)) % 24).toInt()

        val today = Date()
        val diff = today.time - date.time
        val day: Int = ((diff / (1000 * 60 * 60 * 24)).toInt())
        val hours: Int = ((diff / (1000 * 60 * 60)).toInt())
        val minutes: Int = ((diff / (1000 * 60)).toInt())
        if (hours > 0) {
            if (hours == 1)
                return "1 hr, ago"
            else if (hours < 24)
                return (hours).toString() + " hr, ago"
            else {
                //val days = Math.ceil((hours % 24).toDouble()).toInt()
                if (day == 1)
                    return "1 day ago"
                else
                    return (day).toString() + " days ago"
            }
        } else {
            if (minutes == 0)
                return "Just now"
            else if (minutes == 1)
                return "1 min, ago"
            else
                return (minutes).toString() + " min, ago"
        }
    }

    fun deleteFolder(path: String) {
        var file = File(path)
        if (file.exists()) {
            var deleteCmd = "rm -r $path"
            val runtime = Runtime.getRuntime()
            runtime.exec(deleteCmd)
        }
    }

    fun convertGMTT_Local(ourDate: String): String {
        var ourDate = ourDate
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)

            //Log.d("ourDate", ourDate);
        } catch (e: Exception) {
            ourDate = "00-00-0000 00:00"
        }

        return ourDate
    }

    fun getText(editText: AppCompatEditText): String {
        return editText.text.toString()
    }

    fun getYourPointImages(ua_type: String): Int {
        var images = 0
        when (ua_type) {
            "completing_daily_practice" -> {

                images = R.drawable.ic_daily_practice_violation
                return images
            }
            "shared_practice" -> {
                images = R.drawable.ic_share_second
                return images
            }
            "shared_journal_prompt_of_the_day" -> {
                images = R.drawable.ic_journal_prompt
                return images
            }
            "shared_racipe_food_photo" -> {
                images = R.drawable.ic_shared_recipe
                return images
            }
            "shared_months_calendar" -> {
                images = R.drawable.ic_shared_month_calender
                return images
            }
            else -> {
                images = R.drawable.ic_invite_friends
                return images
            }
        }
    }

    fun getYourPointText(ua_type: String, uaEarnedPoints: Int): String {
        var text = ""
        when (ua_type) {
            "completing_daily_practice" -> {

                text = "You have earned $uaEarnedPoints points by completing daily practice"
                return text
            }
            "shared_practice" -> {
                text = "You have shared daily practice"
                return text
            }
            "shared_journal_prompt_of_the_day" -> {
                text = "You have shared journal prompt of the day"
                return text
            }
            "shared_racipe_food_photo" -> {
                text = "You have shared recipe food photo"
                return text
            }
            "shared_months_calendar" -> {
                text = "You have shared month's calendar"
                return text
            }
            else -> {
                text = "Invite your friends to join the app!"
                return text
            }
        }
    }

    // basic subscription benefits // its static
    fun getBasicBenefits(): ArrayList<Benefit> {
        val basicBenefits: ArrayList<Benefit> = ArrayList()
        basicBenefits.add(Benefit("Calender"))
        basicBenefits.add(Benefit("Video library"))
        basicBenefits.add(Benefit("Receipes"))
        basicBenefits.add(Benefit("Leaderboard", false))
        basicBenefits.add(Benefit("Community", false))
        basicBenefits.add(Benefit("Points & options to share", false))

        return basicBenefits
    }

    // premium subscription benefits // its static
    fun getPremiumBenefits(): ArrayList<Benefit> {
        val premiumBenefits: ArrayList<Benefit> = ArrayList()
        premiumBenefits.add(Benefit("Calender"))
        premiumBenefits.add(Benefit("Video library"))
        premiumBenefits.add(Benefit("Receipes"))
        premiumBenefits.add(Benefit("Leaderboard"))
        premiumBenefits.add(Benefit("Community"))
        premiumBenefits.add(Benefit("Points & options to share"))

        return premiumBenefits
    }


    fun getFiresafeNotifications(prefs: Prefs): String {
        var token: String = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FIREBASE", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                token = task.result?.token.toString()
                prefs.firebaseToken = token
                Log.e("FIREBASE", "-->" + token)

            })
        return token
    }

    // getting user subscription type
    fun getUserSubscription(context: Context): Int {
        val appDatabase = getAppDatabase(context)
        val userSubscription = getUserCurrentSubscription(context, appDatabase)
        if (userSubscription == null) {
            return AppConstants.NO_SUBSCRIPTION
        } else {
            if (userSubscription.usSubscriptionPlanId!!.startsWith("basic")) {
                return AppConstants.BASIC_SUBSCRIPTION
            } else if (userSubscription.usSubscriptionPlanId!!.startsWith("premium") ||
                userSubscription.usSubscriptionPlanId!!.equals("trial", true)
            ) {
                return AppConstants.PREMIUM_SUBSCRIPTION
            }
        }
        return AppConstants.NO_SUBSCRIPTION
    }

    fun getUserCurrentSubscription(context: Context, appDatabase: AppDatabase): UserSubscription? {
        val user = Prefs.getInstance(context)!!.userDataModel
        user?.let {
            it.result?.let { userData ->
                val userSubscriptions =
                    appDatabase.userSubscriptionDao().getUserSubscriptionData(userData.uId!!)
                if (userSubscriptions.isEmpty()) {
                    return null
                } else {
                    return userSubscriptions[0]
                }
            }
        }
        return null
    }

    private fun getAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppConstants.DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }


    fun isAppOnForeground(context: Context, appPackageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == appPackageName) {
                Log.e("app", appPackageName + "-->Forgraound")
                return true
            } else {
                Log.e("app", appPackageName + "-->Background")
            }
        }
        return false
    }

    fun getYourPointPer(points: Int): Int {
        var point = points
        when {
            points <= 500 -> {
                point = (100 * point) / 500
                Log.e("POINTS", "->" + point)
                return point
            }
            points in 501..1200 -> {
                point = (100 * point) / 1200
                Log.e("POINTS", "->" + point)
                return point
            }
            points in 1201..2000 -> {
                point = (100 * point) / 2000
                Log.e("POINTS", "->" + point)
                return point
            }
            points in 2001..2800 -> {
                point = (100 * point) / 2800
                Log.e("POINTS", "->" + point)
                return point
            }
            else -> {
                point = 100
            }
        }
        return point
    }

    fun getYourPointLeft(
        points: Int,
        iv: AppCompatImageView,
        context: Context
    ): Int {
        val pointsCondition = Prefs.getInstance(context)!!.settings.result!!.point_system
        val beginnerRange = IntRange(
            pointsCondition!!.beginner!!.split("-")[0].toInt(),
            pointsCondition.beginner!!.split("-")[1].toInt()
        )
        val maidenRange = IntRange(
            pointsCondition.maiden!!.split("-")[0].toInt(),
            pointsCondition.maiden!!.split("-")[1].toInt()
        )
        val priestessRange = IntRange(
            pointsCondition.priestess!!.split("-")[0].toInt(),
            pointsCondition.priestess!!.split("-")[1].toInt()
        )
        val queenRange = IntRange(
            pointsCondition.queen!!.split("-")[0].toInt(),
            pointsCondition.queen!!.split("-")[1].toInt()
        )

        var point = points
        when {
            points in beginnerRange -> {
                point = 500 - points
                Log.e("POINTS", "->" + point)
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_maiden))

                return point
            }
            points in maidenRange -> {
                point = 500 - points
                Log.e("POINTS", "->" + point)
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_maiden))

                return point
            }
            points in priestessRange -> {
                point = 1200 - points
                Log.e("POINTS", "->" + point)
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_priestess))

                return point
            }
            points in queenRange -> {
                point = 2000 - points
                Log.e("POINTS", "->" + point)
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_queen))

                return point
            }
            else -> {
                point = pointsCondition.goddess!!.toInt() - points
                Log.e("POINTS", "->" + point)
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_goddess))

                return point
            }
        }
    }

    fun setImages(
        points: Int,
        iv: AppCompatImageView,
        context: Context, isSetBigIcon: Boolean = false
    ) {
        val pointsCondition = Prefs.getInstance(context)!!.settings.result!!.point_system
        val beginnerRange = IntRange(
            pointsCondition!!.beginner!!.split("-")[0].toInt(),
            pointsCondition.beginner!!.split("-")[1].toInt()
        )
        val maidenRange = IntRange(
            pointsCondition.maiden!!.split("-")[0].toInt(),
            pointsCondition.maiden!!.split("-")[1].toInt()
        )
        val priestessRange = IntRange(
            pointsCondition.priestess!!.split("-")[0].toInt(),
            pointsCondition.priestess!!.split("-")[1].toInt()
        )
        val queenRange = IntRange(
            pointsCondition.queen!!.split("-")[0].toInt(),
            pointsCondition.queen!!.split("-")[1].toInt()
        )

        when (points) {
            in beginnerRange -> {
                //  iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_maiden))
                iv.visibility = View.INVISIBLE
            }
            in maidenRange -> {
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_crown_maiden))
                iv.visibility = View.VISIBLE
            }
            in priestessRange -> {
                iv.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_crown_priestess
                    )
                )
                iv.visibility = View.VISIBLE
            }
            in queenRange -> {
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_crown_queen))
                iv.visibility = View.VISIBLE
            }
            else -> {
                if (isSetBigIcon) {

                    iv.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_crown_goddess
                        )
                    )
                } else {
                    iv.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_crown_goddess_small
                        )
                    )
                }
                iv.visibility = View.VISIBLE
            }
        }
    }

    fun setName(
        context: Context,
        points: Int,
        text: AppCompatTextView
    ) {
        val pointsCondition = Prefs.getInstance(context)!!.settings.result!!.point_system
        val beginnerRange = IntRange(
            pointsCondition!!.beginner!!.split("-")[0].toInt(),
            pointsCondition.beginner!!.split("-")[1].toInt()
        )
        val maidenRange = IntRange(
            pointsCondition.maiden!!.split("-")[0].toInt(),
            pointsCondition.maiden!!.split("-")[1].toInt()
        )
        val priestessRange = IntRange(
            pointsCondition.priestess!!.split("-")[0].toInt(),
            pointsCondition.priestess!!.split("-")[1].toInt()
        )
        val queenRange = IntRange(
            pointsCondition.queen!!.split("-")[0].toInt(),
            pointsCondition.queen!!.split("-")[1].toInt()
        )

        when (points) {
            in beginnerRange -> {
                text.text = "Beginner"
            }
            in maidenRange -> {
                text.text = "Maiden"
            }
            in priestessRange -> {
                text.text = "Priestess"
            }
            in queenRange -> {
                text.text = "Queen"
            }
            else -> {
                text.text = "Goddess"
            }
        }
    }


    // getting appropriate delivery charge
    fun getDeliveryCharges(context: Context, subAmount: Double, country: String?): Double {
        var deliveryCharge = 0.0

        val settings = Prefs.getInstance(context)!!.settings
        // this array contains the list of delivery charges objects
        // for ex. if price is in beterrn 150-200 then apply this delivery charge etc
        val deliveryCharges = settings.result!!.order_delivery_charge

        var deliveryChargeToApply: AppSettingsResponse.Result.OrderDeliveryCharge? = null

        deliveryCharges?.let {
            for (charge in deliveryCharges) {
                // if maxPrice ==0 -> then consider unlimited
                if (charge!!.max_order_value!! != 0) {
                    if (subAmount > charge.min_order_value!! && subAmount <= charge.max_order_value!!) {
                        deliveryChargeToApply = charge
                        //break
                        if (country == null) {
                            deliveryCharge =
                                deliveryChargeToApply?.local_delivery_charge!!.toDouble()
                        } else {
                            if (country.equals("Canada", true) || country.equals("USA")) {
                                deliveryCharge =
                                    deliveryChargeToApply?.local_delivery_charge!!.toDouble()
                            } else {
                                deliveryCharge =
                                    deliveryChargeToApply?.international_delivery_charge!!.toDouble()
                            }
                        }
                        return deliveryCharge
                    }
                } else {
                    if (subAmount >= charge.min_order_value!!) {
                        deliveryChargeToApply = charge
                        //break

                        if (country == null) {
                            deliveryCharge =
                                deliveryChargeToApply?.local_delivery_charge!!.toDouble()
                        } else {
                            if (country.equals("Canada", true) || country.equals("USA")) {
                                deliveryCharge =
                                    deliveryChargeToApply?.local_delivery_charge!!.toDouble()
                            } else {
                                deliveryCharge =
                                    deliveryChargeToApply?.international_delivery_charge!!.toDouble()
                            }
                        }
                        return deliveryCharge
                    }
                }

            }
        }

        return deliveryCharge
    }

    // To detect free trial is used or not
    fun isFreeTrialUsed(context: Context): Boolean {
        val appDatabase = getAppDatabase(context)
        val userSubscription = getUserCurrentSubscription(context, appDatabase)

        if (userSubscription != null) {
            return true
        } else {
            val expiredSubscription = getExpiredUserCurrentSubscription(context, appDatabase)
            return expiredSubscription != null
        }

    }

    // Getting expired subscriptions of user
    // This will helpful to detect should we show the free trial button or not
    fun getExpiredUserCurrentSubscription(
        context: Context,
        appDatabase: AppDatabase
    ): UserSubscription? {
        val user = Prefs.getInstance(context)!!.userDataModel
        user?.let {
            it.result?.let { userData ->
                val userSubscriptions =
                    appDatabase.userSubscriptionDao().getExpiredSubscriptionData(userData.uId!!)
                if (userSubscriptions.isEmpty()) {
                    return null
                } else {
                    return userSubscriptions[0]
                }
            }
        }
        return null
    }

    // function to save social media Profile picture
    // and then send this image in the API
    fun createImageFile(context: Context, bitmap: Bitmap, from: String): String {
        var newImagePath = ""
        val dirPath = context.filesDir.absolutePath + File.separator + "PROFILE_IMAGES"
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        var fileName = ""
        if (from.equals("facebook", true)) {
            fileName = "fb_profile.jpg"
        } else if (from.equals("google", true)) {
            fileName = "google_profile.jpg"
        } else {
            fileName = "profile_" + UUID.randomUUID() + ".jpg"
        }
        // Create a file to save the image
        val imgFile = File(dir, fileName)

        try {
            // Initialize a new OutputStream
            var stream: OutputStream? = null
            // If the output file exists, it can be replaced or appended to it
            stream = FileOutputStream(imgFile)
            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flushes the stream
            stream.flush();
            // Closes the stream
            stream.close();
        } catch (e: IOException) {
            Log.e("SAVE_IMAGE", "" + e.message)
        }

        newImagePath = imgFile.absolutePath

        return newImagePath
    }

    fun getVideoUrl(path: String?): String {
        if (path != null && path.isNotBlank()) {
            return BuildConfig.VIDEO_BASE_URL + path
        }
        return ""
    }
}