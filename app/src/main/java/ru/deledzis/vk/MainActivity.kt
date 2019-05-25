package ru.deledzis.vk

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ru.deledzis.vk.list.controller.IMediaPlayerController
import ru.deledzis.vk.list.model.Track
import ru.deledzis.vk.list.view.FragmentTracksList
import ru.deledzis.vk.player.FragmentBigPlayer
import java.io.IOException

class MainActivity : AppCompatActivity(), IMediaPlayerController {

    private val tag = this.javaClass.name

    private lateinit var mCurrentTrack: Track
    private var mCurrentTrackIndex = -1
    private var mMediaPlayer: MediaPlayer? = null
    private var mLooping: Boolean = false

    val isPlaying: Boolean
        get() {
            return mMediaPlayer?.isPlaying ?: false
        }

    val duration: Long
        get() = mMediaPlayer?.duration?.toLong() ?: -1L

    val mediaPlayerPosition: Int
        get() = mMediaPlayer?.currentPosition ?: -1

    val currentTrack: Track
        get() = mCurrentTrack

    private lateinit var mFragmentTracksList: FragmentTracksList
    private var mFragmentBigPlayer: FragmentBigPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFragmentTracksList = FragmentTracksList.newInstance()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_holder, mFragmentTracksList)
            .commit()

        mMediaPlayer = MediaPlayer()
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    fun showBigPlayerFragment() {
        mFragmentBigPlayer = FragmentBigPlayer.newInstance().apply {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up_animation, 0, 0, R.anim.slide_down_animation)
                .add(R.id.fragment_holder, this)
                .addToBackStack(null)
                .commit()
        }
    }

    fun dismissBigPlayerFragment() {
        supportFragmentManager.popBackStackImmediate()
        mFragmentBigPlayer = null
    }

    fun startPlayer(track: Track, index: Int) {
        mCurrentTrackIndex = index
        mCurrentTrack = track
        releaseMediaPlayer()
        prepareMediaPlayer()
    }

    fun pausePlayer() {
        mMediaPlayer?.let {
            if (it.trackInfo != null) {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        }
    }

    fun resumePlayer() {
        mMediaPlayer?.let {
            try {
                if (!it.isPlaying) {
                    it.start()
                    mFragmentTracksList.handleTrackStart()
                    mFragmentBigPlayer?.handleTrackStart()
                }
            } catch (e: RuntimeException) {
                restartPlayer()
            }
        }
    }

    private fun restartPlayer() {
        prepareMediaPlayer()
    }

    fun previousTrack() {
        val tracks = mFragmentTracksList.tracks
        mCurrentTrackIndex = if (mCurrentTrackIndex - 1 < 0) tracks.size - 1 else mCurrentTrackIndex - 1
        mCurrentTrack = tracks[mCurrentTrackIndex]
        releaseMediaPlayer()
        prepareMediaPlayer()
    }

    fun nextTrack() {
        val tracks = mFragmentTracksList.tracks
        mCurrentTrackIndex = if (mCurrentTrackIndex + 1 >= tracks.size) 0 else mCurrentTrackIndex + 1
        mCurrentTrack = tracks[mCurrentTrackIndex]
        releaseMediaPlayer()
        prepareMediaPlayer()
    }

    override fun prepareMediaPlayer() {
        try {
            if (mMediaPlayer == null) mMediaPlayer = MediaPlayer()
            mMediaPlayer?.let {
                it.setDataSource(this, mCurrentTrack.uri)
                it.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                it.isLooping = mLooping
                it.setOnPreparedListener(this)
                it.setOnCompletionListener(this)
                it.prepare()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun releaseMediaPlayer() {
        try {
            mMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.release()
                }
            }
            mMediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekBarProgressChanged(fromUser: Boolean, progress: Int) {
        if (fromUser) {
            mMediaPlayer?.seekTo(progress * 1000)
        }
    }

    fun handleTrackAdded(added: Boolean) {
        mFragmentTracksList.tracks[mCurrentTrackIndex].added = added
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCompletion(mp: MediaPlayer) {
        mMediaPlayer?.reset()
        mFragmentTracksList.handleTrackEnd()
        mFragmentBigPlayer?.handleTrackEnd()
        nextTrack()
    }

    override fun onPrepared(mp: MediaPlayer) {
        mMediaPlayer?.start()
        mFragmentTracksList.handleTrackStart()
        mFragmentBigPlayer?.handleTrackStart()
    }
}
