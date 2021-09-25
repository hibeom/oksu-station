package com.pinkcloud.oksupilot

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.pinkcloud.oksupilot.databinding.FragmentContentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun playbackStateListener(scrollView: ScrollView): Player.EventListener {
    return object : Player.EventListener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == ExoPlayer.STATE_ENDED) {
                scrollView.setOnTouchListener(null)
            }
        }
    }
}

class ContentFragment : Fragment() {

    private lateinit var viewBinding: FragmentContentBinding

    private var player: SimpleExoPlayer? = null
    private lateinit var playbackStateListener: Player.EventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentContentBinding.inflate(inflater, container, false)

        initializePlayer()
        playbackStateListener = playbackStateListener(viewBinding.scrollView)

        val screenHeight = requireContext().resources.displayMetrics.heightPixels
        var isTriggered = false
        viewBinding.scrollView.viewTreeObserver.addOnScrollChangedListener {

            if (viewBinding.scrollView.scrollY + screenHeight > viewBinding.videoView.top) {
                if (!isTriggered) {
                    viewBinding.scrollView.smoothScrollBy(0, 0)
                    viewBinding.scrollView.setOnTouchListener { v, event -> true }
                    ObjectAnimator.ofInt(
                        viewBinding.scrollView,
                        "scrollY",
                        viewBinding.videoView.top
                    ).setDuration(800).start()
                    lifecycleScope.launch {
                        delay(600)
                        player?.let {
                            it.playWhenReady = true
                            it.addListener(playbackStateListener)
                            it.prepare()
                        }
                    }
                    isTriggered = true
                }
            } else {
                if (isTriggered) {
                    player?.let {
                        it.removeListener(playbackStateListener)
                        it.playWhenReady = false
                        it.seekTo(0, 0L)
                    }
                    isTriggered = false
                }
            }
        }

        return viewBinding.root
    }

    private fun initializePlayer() {

        player = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also {
                viewBinding.videoView.player = it
                val rawDataSource = RawResourceDataSource(requireContext())
                rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.hand_movement)))
                val mediaItem = MediaItem.fromUri(rawDataSource.uri!!)

                it.setMediaItem(mediaItem)
                it.seekTo(0, 0L)
            }
    }

    private fun releasePlayer() {
        player?.run {
            release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        hideSystemUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        showSystemUi()
    }

    private fun hideSystemUi() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUi() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window,
            viewBinding.root
        ).show(WindowInsetsCompat.Type.systemBars())
    }
}