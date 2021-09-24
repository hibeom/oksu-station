package com.pinkcloud.oksupilot

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.pinkcloud.oksupilot.databinding.FragmentContentBinding
import java.io.File

class ContentFragment : Fragment() {

    private lateinit var viewBinding: FragmentContentBinding

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var player: SimpleExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentContentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = SimpleExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector)
            .build()
            .also {
                viewBinding.videoView.player = it
//                val videoUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "//" + requireContext().packageName + "/" + R.raw.hand_movement)
//                Log.d("devlog", "uri: $videoUri")
//                val mediaItem = MediaItem.fromUri(videoUri)
                val rawDataSource = RawResourceDataSource(requireContext())
                rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.hand_movement)))
                val mediaItem = MediaItem.fromUri(rawDataSource.uri!!)

                it.setMediaItem(mediaItem)
                it.playWhenReady = playWhenReady
                it.seekTo(currentWindow, playbackPosition)
                it.prepare()
            }
    }

    private fun releasePlayer() {
        player?.run {
            playWhenReady = this.playWhenReady
            currentWindow = this.currentWindowIndex
            playbackPosition = this.currentPosition
            release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
}