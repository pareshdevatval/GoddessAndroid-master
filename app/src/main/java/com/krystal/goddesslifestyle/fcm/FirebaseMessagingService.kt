package com.krystal.goddesslifestyle.fcm

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.Voice
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.data.Prefs
import com.krystal.goddesslifestyle.data.Prefs.Companion.prefs
import com.krystal.goddesslifestyle.ui.activity.DialogActivity
import com.krystal.goddesslifestyle.ui.activity.NotificationActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

import org.json.JSONObject
import java.util.*


class FirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "GODDESS_APP"
    lateinit var intent: Intent

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "firebase token->$token")


    }

    companion object {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        prefs = Prefs.getInstance(this)!!
        Log.i(TAG, "MessageReceived" + remoteMessage.data)
        remoteMessage.let { message ->
            val map: Map<String, String> = message.data


            if (map.containsKey("android")) {

                val json = JSONObject(map["android"])
                Log.e("FIREBASE_MSG", "--->" + json)
             //   showPushNotification(json)
                if (!AppUtils.isAppOnForeground(this, applicationContext.packageName)) {
                    Log.e("BACKGROUND", "---->TRUE")
                    showPushNotification(json)
                } else {
                    Log.e("BACKGROUND", "---->FALSE")
                    openDialog(json)
                }
            }
        }


    }
    private fun openDialog(json: JSONObject) {
        startActivity(
            DialogActivity.newInstant(
                this,
                json.getString("message"),
               "Notification"
            )
        )
    }
    private fun showPushNotification(json: JSONObject) {
        intent = NotificationActivity.newInstance(this)
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupNotificationChannels()
        }
        val notificationId = Random().nextInt(60000)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)  //a resource for your custom small icon
            .setContentTitle(/*json.getString("title")*/"Goddess LifeStyle") //the "title" value you sent in your notification
            .setContentText(json.getString("message")) //ditto
            .setAutoCancel(true)  //dismisses the notification on click
            .setSound(defaultSoundUri)
            // .setColor(resources.getColor(R.color.red_1, theme))
            .setColor(ContextCompat.getColor(this, R.color.pink))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            notificationId /* ID of notification */,
            notificationBuilder.build()
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val adminChannelName = "chanel"
        val adminChannelDescription = "chanell"
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        adminChannel.setSound(defaultSoundUri, audioAttributes)
        notificationManager.createNotificationChannel(adminChannel)
    }


}