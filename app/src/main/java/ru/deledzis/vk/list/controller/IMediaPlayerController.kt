package ru.deledzis.vk.list.controller

import android.media.MediaPlayer

interface IMediaPlayerController : MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    fun prepareMediaPlayer()

    fun releaseMediaPlayer()
}