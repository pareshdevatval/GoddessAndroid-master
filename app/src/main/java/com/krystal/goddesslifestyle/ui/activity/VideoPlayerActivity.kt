package com.krystal.goddesslifestyle.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.databinding.ActivityVideoPlayerBinding



class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            return intent
        }
    }

    // binding variable
    lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: SimpleExoPlayer? = null

    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition = 0L


    val TAG = VideoPlayerActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
    }

    private fun initializePlayer() {
        // exoPlayer intialization
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        // to bind the player to its corresponding view.
        binding.playerView.player = exoPlayer

        val uri = Uri.parse(getString(R.string.media_url_video))
        val mediaSource = buildMediaSource(uri)

        // tells the player whether to start playing as soon as all resources for playback have been acquired.
        // Since playWhenReady is initially true, playback will start automatically the first time the app is run.
        exoPlayer?.playWhenReady = playWhenReady
        // tells the player to seek to a certain position within a specific window.
        // Both currentWindow and playbackPosition were initialized to zero so that playback starts from the
        // very start the first time the app is run.
        exoPlayer?.seekTo(currentWindow, playbackPosition)

        // To have our callbacks called we need to register our playbackStateListener with the player.
        // Let's do that in initializePlayer.
        playbackStateListener?.let {
            exoPlayer?.addListener(it)
        }
        // tells the player to acquire all the resources for the given mediaSource,
        // and additionally tells it not to reset the position or state,
        // since we already set these in the two previous lines.
        exoPlayer?.prepare(mediaSource, false, false)
    }

    /**
     * @param uri -> containing the URI of a media file.
     * */
    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "exoplayer-codelab")
        // a factory for creating progressive media data sources
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 || exoPlayer == null)) {
            initializePlayer();
        }
    }

    // To have a full screen experience.
    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        binding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private fun releasePlayer() {
        exoPlayer?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex

            // Again, we need to tidy up to avoid dangling references from the player which could cause a memory leak.
            exoPlayer?.removeListener(playbackStateListener)
            it.release()
            exoPlayer = null
        }
    }

    var playbackStateListener : PlaybackStateListener? = null

    class PlaybackStateListener: Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            var stateString: String = ""

            when(playbackState) {
                // The player has been instantiated but has not yet been prepared with a MediaSource.
                ExoPlayer.STATE_IDLE -> {
                    stateString = "ExoPlayer.STATE_IDLE      -"
                }
                // The player is not able to play from the current position because not enough data has been buffered.
                ExoPlayer.STATE_BUFFERING -> {
                    stateString = "ExoPlayer.STATE_BUFFERING      -"
                }
                // The player is able to immediately play from the current position.
                // This means the player will start playing media automatically if playWhenReady is true.
                // If it is false the player is paused.
                ExoPlayer.STATE_READY -> {
                    stateString = "ExoPlayer.STATE_READY      -"
                }
                // The player has finished playing the media.
                ExoPlayer.STATE_ENDED -> {
                    stateString = "ExoPlayer.STATE_ENDED      -"
                }
                else -> {
                    stateString = "UNKNOWN_STATE"
                }
            }
            Log.d("VideoPlayerActivity", "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }
    }
}
