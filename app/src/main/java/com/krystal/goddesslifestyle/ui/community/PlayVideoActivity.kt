package com.krystal.goddesslifestyle.ui.community

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseActivity
import com.krystal.goddesslifestyle.databinding.ActivityPlayVideoBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.viewmodels.CommentsViewModel
import com.krystal.goddesslifestyle.viewmodels.PlayVideoViewModel

class PlayVideoActivity : BaseActivity<PlayVideoViewModel>(),
    Player.EventListener{

    private lateinit var mViewModel: PlayVideoViewModel
    private lateinit var binding:ActivityPlayVideoBinding

    lateinit var player: SimpleExoPlayer
    private lateinit var videoSource: ProgressiveMediaSource

    private val fileName: String by lazy {
        intent.getStringExtra(AppConstants.VIDEO_URL)
    }

    private val isportrait:Boolean by lazy {
        intent.getBooleanExtra(AppConstants.SCREEN_ORIENTATION,false)
    }
    override fun getViewModel(): PlayVideoViewModel {
        mViewModel = ViewModelProvider(this).get(PlayVideoViewModel::class.java)
        return mViewModel
    }

    companion object {
        fun newInstance(
            context: Context,
            fileName: String,portrait:Boolean=false): Intent {
            val intent = Intent(context, PlayVideoActivity::class.java)
            intent.putExtra(AppConstants.VIDEO_URL, fileName)
            intent.putExtra(AppConstants.SCREEN_ORIENTATION, portrait)
            return intent
        }
    }

    override fun internetErrorRetryClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_play_video)
        /*if (isportrait){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }*/
        manageScreenOrientationChange(this.resources.configuration.orientation)
    }



    override fun onResume() {
        super.onResume()
        playerInit()
    }

    private fun playerInit() {
        player = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultTrackSelector(), DefaultLoadControl()
        )
        player.addListener(this)
        binding.videoView.useController=true
        binding.videoView.overlayFrameLayout
        binding.videoView.player = player
        //binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        val uri =
            Uri.parse(fileName)
        videoPlayer(uri)

    }


    private fun videoPlayer(uri: Uri?) {
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

    override fun onPause() {
        super.onPause()
        player.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                // status = "LOADING"
            }
            Player.STATE_ENDED -> {
                //binding.videoView.useController = false
                // binding.ivPlay.visibility = View.VISIBLE
                //status = "STOPPED"
                binding.videoView.showController()
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
        Log.e("EXO_ERROR", ""+error?.message)
        AppUtils.showToast(this, "Error playing video...")
    }


    override fun onLoadingChanged(isLoading: Boolean) {
        Log.e("TAG", "----------------->4$isLoading")
    }


    override fun onRepeatModeChanged(repeatMode: Int) {
        Log.e("TAG", "----------------->5")
    }
}
