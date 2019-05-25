package ru.deledzis.vk.list.model

import android.graphics.Bitmap
import android.net.Uri

class Track(
    val uri: Uri,
    val artist: String,
    val name: String,
    val duration: Long,
    val bitmap: Bitmap?,
    var added: Boolean
)