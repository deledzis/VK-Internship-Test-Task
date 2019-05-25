package ru.deledzis.vk.list.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.deledzis.vk.R
import ru.deledzis.vk.list.model.Track
import ru.deledzis.vk.util.parseTime

class TracksListAdapter internal constructor(
    private val mContext: Context,
    private val mListener: TracksListAdapterListener,
    private val mTracks: List<Track>
) : RecyclerView.Adapter<TracksListAdapter.ViewHolder>() {
    private val tag = this.javaClass.name

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {

        val view = LayoutInflater.from(mContext).inflate(
            R.layout.fragment_list_item,
            viewGroup,
            false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = mTracks[position]
        holder.mItem = track

        if (track.bitmap != null) {
            holder.mTrackImage.setImageBitmap(track.bitmap)
        } else {
            holder.mTrackImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_broken_image))
        }

        holder.mTrackNameText.text = track.name
        holder.mTrackArtistText.text = track.artist
        holder.mTrackDurationText.text = parseTime(track.duration)

        holder.mView.setOnClickListener { mListener.onTrackClicked(track) }
    }

    override fun getItemCount(): Int {
        return mTracks.size
    }

    inner class ViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
        var mTrackImage: ImageView = mView.findViewById(R.id.list_track_art)
        var mTrackNameText: TextView = mView.findViewById(R.id.list_track_title)
        var mTrackArtistText: TextView = mView.findViewById(R.id.list_track_artist)
        var mTrackDurationText: TextView = mView.findViewById(R.id.list_track_duration_left)

        var mItem: Track? = null
    }

    interface TracksListAdapterListener {
        fun onTrackClicked(track: Track)
    }
}
