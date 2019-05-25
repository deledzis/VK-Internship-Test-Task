package ru.deledzis.vk.player

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_player.view.*
import ru.deledzis.vk.MainActivity
import ru.deledzis.vk.R
import ru.deledzis.vk.base.BaseFragment
import ru.deledzis.vk.util.parseTime

class FragmentBigPlayer : BaseFragment() {
    private val TAG = this.javaClass.name

    private lateinit var mView: View
    private lateinit var mDropDownIcon: ImageView
    private lateinit var mAlbumArtImage: ImageView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mActionIcon: ImageButton
    private lateinit var mPreviousTrackIcon: ImageButton
    private lateinit var mNextTrackIcon: ImageButton
    private lateinit var mAddTrackIcon: ImageButton
    private lateinit var mTrackNameText: TextView
    private lateinit var mTrackArtistText: TextView
    private lateinit var mTrackCurrentTimeText: TextView
    private lateinit var mTrackTimeLeftText: TextView

    private var mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context == null) {
            Log.e(TAG, "[ERROR] Context is null")
        } else {
            mActivity = context as MainActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(
            R.layout.fragment_player,
            container,
            false
        )

        initViews()

        initSeekBar()

        setContent()

        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) { }

            override fun onStartTrackingTouch(seekBar: SeekBar) { }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mActivity.seekBarProgressChanged(fromUser, progress)
            }
        })

        mDropDownIcon.setOnClickListener { mActivity.dismissBigPlayerFragment() }

        mActionIcon.setOnClickListener {
            if (mActivity.isPlaying) {
                mActivity.pausePlayer()
                mActionIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_play_48))
            } else {
                mActivity.resumePlayer()
                mActionIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pause_48))
            }
        }

        mNextTrackIcon.setOnClickListener { mActivity.nextTrack() }

        mPreviousTrackIcon.setOnClickListener { mActivity.previousTrack() }

        mAddTrackIcon.setOnClickListener {
            if (mActivity.currentTrack.added) {
                mActivity.handleTrackAdded(false)
                mAddTrackIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_add_outline_24))
                mActivity.showToast(getString(R.string.track_removed))
            } else {
                mActivity.handleTrackAdded(true)
                mAddTrackIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_done_outline_24))
                mActivity.showToast(getString(R.string.track_added))
            }
        }

        return mView
    }

    private fun setContent() {
        val bitmap = mActivity.currentTrack.bitmap
        if (bitmap != null) {
            mAlbumArtImage.setImageBitmap(bitmap)
        } else {
            mAlbumArtImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_broken_image))
        }

        mActionIcon.setImageDrawable(if (mActivity.isPlaying) {
            mActivity.getDrawable(R.drawable.ic_pause_48)
        } else {
            mActivity.getDrawable(R.drawable.ic_play_48)
        })

        mAddTrackIcon.setImageDrawable(if (mActivity.currentTrack.added) {
            mActivity.getDrawable(R.drawable.ic_done_outline_24)
        } else {
            mActivity.getDrawable(R.drawable.ic_add_outline_24)
        })

        mTrackNameText.text = mActivity.currentTrack.name
        mTrackArtistText.text = mActivity.currentTrack.artist
        mTrackCurrentTimeText.text = parseTime(mActivity.mediaPlayerPosition)
        mTrackTimeLeftText.text = "-${parseTime(mActivity.duration - mActivity.mediaPlayerPosition)}"
    }

    private fun initSeekBar() {
        mSeekBar.max = mActivity.duration.toInt() / 1000
        Log.d(TAG, mSeekBar.max.toString())

        mRunnable = object : Runnable {
            override fun run() {
                if (mActivity.isPlaying) {
                    val position = mActivity.mediaPlayerPosition / 1000
                    mSeekBar.progress = position

                    mTrackCurrentTimeText.text = parseTime(mActivity.mediaPlayerPosition)
                    mTrackTimeLeftText.text = "-${parseTime(mActivity.duration - mActivity.mediaPlayerPosition)}"

                    mHandler.postDelayed(this, 1000)
                } else {
                    removeCallback(this)
                }
            }
        }
        mActivity.runOnUiThread(mRunnable)
    }

    private fun initViews() {
        with(mView) {
            mDropDownIcon = player_dropdown_icon
            mAlbumArtImage = player_album_cover
            mSeekBar = player_current_progress
            mActionIcon = player_action_icon
            mPreviousTrackIcon = player_next
            mNextTrackIcon = player_previous
            mAddTrackIcon = player_add_icon
            mTrackNameText = player_track_name
            mTrackArtistText = player_artist
            mTrackCurrentTimeText = player_current_time
            mTrackTimeLeftText = player_time_left
        }
    }

    fun removeCallback(runnable: Runnable) {
        mHandler.removeCallbacks(runnable)
    }

    fun handleTrackStart() {
        initSeekBar()
        setContent()
    }

    fun handleTrackEnd() {
        mActionIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_play_48))
        mTrackCurrentTimeText.text = parseTime(0)
        mTrackTimeLeftText.text = parseTime(0)
    }

    companion object {
        fun newInstance(): FragmentBigPlayer {
            return FragmentBigPlayer()
        }
    }
}