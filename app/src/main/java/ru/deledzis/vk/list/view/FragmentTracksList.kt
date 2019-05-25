package ru.deledzis.vk.list.view

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.android.synthetic.main.fragment_list_mini_player_layout.view.*
import kotlinx.android.synthetic.main.fragment_list_select_folder_layout.view.*
import ru.deledzis.vk.MainActivity
import ru.deledzis.vk.R
import ru.deledzis.vk.base.BaseFragment
import ru.deledzis.vk.list.model.Track
import ru.deledzis.vk.util.ContentUtils.getFilesListCursor
import ru.deledzis.vk.util.ContentUtils.parseBitmap
import ru.deledzis.vk.util.parseTime

class FragmentTracksList : BaseFragment(), TracksListAdapter.TracksListAdapterListener {
    private val TAG = this.javaClass.name

    private lateinit var mView: View
    private lateinit var mTracksList: RecyclerView
    private lateinit var mSelectFolderButton: Button
    private lateinit var mMiniPlayerTitleText: TextView
    private lateinit var mMiniPlayerTimeLeftText: TextView
    private lateinit var mPauseTrackButton: ImageButton
    private lateinit var mNextTrackButton: ImageButton
    private lateinit var mMiniPlayerImage: ImageView
    private lateinit var mEmptyStateLayout: View
    private lateinit var mMiniPlayerLayout: View

    private lateinit var mTracks: ArrayList<Track>
    private var mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable

    val tracks: ArrayList<Track>
        get() = mTracks

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
            R.layout.fragment_list,
            container,
            false
        )

        initViews()

        mTracks = ArrayList()

        mMiniPlayerLayout.setOnClickListener { mActivity.showBigPlayerFragment() }

        mSelectFolderButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    mActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    mActivity,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_PERMISSIONS
                )
            } else {
                openFolderSelection()
            }
        }

        mPauseTrackButton.setOnClickListener {
            if (mActivity.isPlaying) {
                mActivity.pausePlayer()
                mPauseTrackButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_play_arrow))
            } else {
                mActivity.resumePlayer()
                mPauseTrackButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pause_28))
            }
        }

        mNextTrackButton.setOnClickListener {
            mActivity.nextTrack()
            initMiniPlayer()
        }

        return mView
    }

    private fun initViews() {
        with(mView) {
            mTracksList = track_list
            mSelectFolderButton = open_folder_button
            mMiniPlayerTitleText = mini_player_track_title
            mMiniPlayerTimeLeftText = mini_player_duration_left
            mPauseTrackButton = mini_player_pause_icon
            mNextTrackButton = mini_player_next_icon
            mMiniPlayerImage = mini_player_track_art
            mEmptyStateLayout = inc_empty_state_layout
            mMiniPlayerLayout = inc_mini_player
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                mActivity.showToast(getString(R.string.grant_permissions_error))
                return
            }
        }

        openFolderSelection()
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY) {
            if (data != null) {
                collectTracks(data.data)
                if (mTracks.size > 0) {
                    mTracksList.layoutManager = LinearLayoutManager(mActivity)
                    mTracksList.adapter = TracksListAdapter(mActivity, this, mTracks)
                    mTracksList.visibility = View.VISIBLE
                    mEmptyStateLayout.visibility = View.GONE
                } else {
                    mActivity.showToast(getString(R.string.no_tracks_in_folder))
                }
            }
        }
    }

    private fun collectTracks(uri: Uri?) {
        uri?.let {
            getFilesListCursor(mActivity, it)?.apply {
                while (this.moveToNext()) {
                    val bitmap = parseBitmap(this)
                    if (bitmap != null) {
                        mMiniPlayerImage.setImageBitmap(bitmap)
                    } else {
                        mMiniPlayerImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_broken_image))
                    }

                    val trackUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        this.getLong(4)
                    )

                    val track = Track(
                        trackUri,
                        this.getString(1),
                        this.getString(2),
                        java.lang.Long.parseLong(this.getString(3)),
                        bitmap,
                        false
                    )
                    mTracks.add(track)
                }
            }
        }
    }

    private fun openFolderSelection() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(Intent.createChooser(i, "Choose directory"), REQUEST_CODE_OPEN_DIRECTORY)
    }

    private fun initMiniPlayer() {
        mMiniPlayerLayout.visibility = View.VISIBLE
        mMiniPlayerTitleText.text = mActivity.currentTrack.name
        mMiniPlayerTimeLeftText.text = "-${parseTime(mActivity.duration)}"
    }

    override fun onTrackClicked(track: Track) {
        mActivity.startPlayer(track, mTracks.indexOf(track))
        initMiniPlayer()
    }

    fun handleTrackStart() {
        mPauseTrackButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_pause_28))
        mRunnable = object : Runnable {
            override fun run() {
                if (mActivity.isPlaying) {
                    val time = mActivity.duration - mActivity.mediaPlayerPosition
                    mMiniPlayerTimeLeftText.text = "-${parseTime(time)}"

                    mHandler.postDelayed(this, 1000)
                } else {
                    removeCallback(this)
                }
            }
        }
        mActivity.runOnUiThread(mRunnable)
    }

    fun handleTrackEnd() {
        mPauseTrackButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_play_arrow))
        mMiniPlayerTimeLeftText.text = parseTime(0)
    }

    fun removeCallback(runnable: Runnable) {
        mPauseTrackButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_play_arrow))
        mHandler.removeCallbacks(runnable)
    }

    companion object {
        fun newInstance(): FragmentTracksList {
            return FragmentTracksList()
        }

        private const val REQUEST_CODE_OPEN_DIRECTORY = 1
        private const val REQUEST_CODE_PERMISSIONS = 2
    }
}
