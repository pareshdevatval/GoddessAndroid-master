package com.krystal.goddesslifestyle.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseFragment
import com.krystal.goddesslifestyle.databinding.FragmentTutorialVideoBinding
import com.krystal.goddesslifestyle.ui.activity.LoginActivity
import com.krystal.goddesslifestyle.ui.activity.SignUpActivity
import com.krystal.goddesslifestyle.utils.AppUtils
import com.krystal.goddesslifestyle.utils.WaveView
import com.krystal.goddesslifestyle.viewmodels.TutorialVideoViewModel


/**
 * Created by imobdev on 21/2/20
 */
class TutorialVideoFragment : BaseFragment<TutorialVideoViewModel>(), Player.EventListener {

    private lateinit var mViewModel: TutorialVideoViewModel
    private lateinit var binding: FragmentTutorialVideoBinding
    lateinit var player: SimpleExoPlayer
    private lateinit var videoSource: ProgressiveMediaSource

    companion object {
        fun newInstance() = TutorialVideoFragment().apply {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentTutorialVideoBinding.inflate(inflater, container, false)
        binding.videoFragment=this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun getViewModel(): TutorialVideoViewModel {
        mViewModel = ViewModelProvider(this).get(TutorialVideoViewModel::class.java)
        return mViewModel
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onResume() {
        super.onResume()
        playerInit()
    }

    private fun init() {
        setWaveAnimation(R.color.color_bg_pink)
    }

    fun startLoginActivity(){
        context.let {
            it!!.startActivity(LoginActivity.newInstance(context!!))
            AppUtils.startFromRightToLeft(it)
        }
    }

    fun startSignUpActivity(){
        context.let {
            it!!.startActivity(SignUpActivity.newInstance(context!!))
        }
    }


    private fun playerInit() {
        player = ExoPlayerFactory.newSimpleInstance(
            context!!,
            DefaultTrackSelector(), DefaultLoadControl()
        )
        player.addListener(this)
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        binding.videoView.player = player
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        val videoPath = RawResourceDataSource.buildRawResourceUri(R.raw.intro).toString()
        //val uri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4")
        val uri =
            Uri.parse(videoPath)

        videoPlayer(uri)
    }

    fun videoStartStop(isStart: Boolean) {
        /*if (isStart) {
            binding.videoView.resume()
        } else {
            binding.videoView.pause()
        }*/

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                // status = "LOADING"
            }
            Player.STATE_ENDED -> {
                binding.videoView.useController = false

                player.seekTo(0);
                player.playWhenReady = true;

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

    private fun videoPlayer(uri: Uri?) {
        val dataSourceFactory = DefaultDataSourceFactory(
            context!!,
            Util.getUserAgent(context!!, "Goddess")
        )
// This is the MediaSource representing the media to be played.
        videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
// Prepare the player with the source.
        player.prepare(videoSource)
        player.playWhenReady = true
    }


    /* Wave Animation */
    @SuppressLint("NewApi")
    private fun setWaveAnimation(color: Int) {
        /* Left to Right */
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 20).toFloat(),
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
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 20).toFloat(),
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
        binding.wave.addWaveData(
            WaveView.WaveData(
                (800 + Math.random() * 100).toFloat(),
                (100 + Math.random() * 20).toFloat(),
                (5 + Math.random() * 20).toFloat(),
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
        binding.wave.startAnimation()
    }

    override fun onPause() {
        super.onPause()
        player.release()
    }

}