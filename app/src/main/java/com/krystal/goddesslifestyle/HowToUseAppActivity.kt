package com.krystal.goddesslifestyle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.base.BaseViewModel
import com.krystal.goddesslifestyle.databinding.ActivityHowToUseAppBinding
import com.krystal.goddesslifestyle.ui.main_activity.MainActivity
import com.krystal.goddesslifestyle.ui.shop.OrderReviewActivity
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

class HowToUseAppActivity : BaseActivity<BaseViewModel>(), Player.EventListener,
    View.OnClickListener {

    companion object {
        /*Here, tabIndex is the index of the tab to select when activity starts*/
        fun newInstance(context: Context, isFrom: String): Intent {
            val intent = Intent(context, HowToUseAppActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_FROM, isFrom)
            return intent
        }
    }

    /*ViewModel*/
    private lateinit var vModel: BaseViewModel

    /*binding variable*/
    private lateinit var binding: ActivityHowToUseAppBinding

    var from: String = ""

    lateinit var player: SimpleExoPlayer
    private lateinit var videoSource: ProgressiveMediaSource

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_how_to_use_app)
        super.onCreate(savedInstanceState)
        setToolbar()
        manageScreenOrientationChange(this.resources.configuration.orientation)
    }

    override fun getViewModel(): BaseViewModel {
        vModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun setToolbar() {
        setToolbarTitle("")
        setToolbarColor(R.color.black)

        setToolbarLeftIcon(R.drawable.ic_back, object : ToolbarLeftImageClickListener {
            override fun onLeftImageClicked() {
                onBackPressed()
            }
        })
    }

    private fun initVideoPlayer() {
        intent?.let {
            it.getStringExtra(AppConstants.EXTRA_FROM)?.let { extraFrom ->
                from = extraFrom
            }
        }
        binding.btnSkipVideo.setOnClickListener(this)
        player = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultTrackSelector(), DefaultLoadControl()
        )
        player.addListener(this)
        binding.videoView.useController = true
        binding.videoView.player = player

        val uri =
            Uri.parse("http://goddess.reviewprototypes.com/public/uploads/intro/app-welcome.mp4")

        playVideo(uri)
    }

    private fun playVideo(uri: Uri?) {
        val dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "Goddess")
        )
// This is the MediaSource representing the media to be played.
        videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
// Prepare the player with the source.
        player.prepare(videoSource)
        player.playWhenReady = true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnSkipVideo -> {
                moveToHomeScreen()
            }
        }
    }

    private fun moveToHomeScreen() {
        player.release()
        if (from.equals(AppConstants.FROM_CART, true)) {
            startActivity(OrderReviewActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finish()
        } else {
            startActivity(MainActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finishAffinity()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                // status = "LOADING"

            }
            Player.STATE_ENDED -> {
                moveToHomeScreen()
                // binding.ivPlay.visibility = View.VISIBLE
                //status = "STOPPED"

            }
            Player.STATE_IDLE -> {
                // status = "IDLE"
            }
            Player.STATE_READY -> {
                //status = if (playWhenReady) "PLAYING" else "PAUSED"
            }
            else -> {
                //status = "IDLE"
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        manageScreenOrientationChange(newConfig.orientation)
    }

    private fun manageScreenOrientationChange(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.videoView.layoutParams.height = AppUtils.getScreenHeight(this)
            binding.videoView.layoutParams.width = AppUtils.getScreenWidth(this)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.videoView.layoutParams.height = AppUtils.getScreenWidth(this)
            binding.videoView.layoutParams.width = AppUtils.getScreenHeight(this)
        }
    }


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Log.e("TAG", "----------------->1")
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        Log.e("TAG", "----------------->2")

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Log.e("TAG", "----------------->3")

    }


    override fun onLoadingChanged(isLoading: Boolean) {
        Log.e("TAG", "----------------->4$isLoading")

    }


    override fun onRepeatModeChanged(repeatMode: Int) {
        Log.e("TAG", "----------------->5")

    }

    override fun onPause() {
        super.onPause()
        player.release()
    }

    override fun onResume() {
        super.onResume()
        initVideoPlayer()
    }


}
