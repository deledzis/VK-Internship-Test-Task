package ru.deledzis.vk.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
import android.util.Log

object ContentUtils {

    fun getFilesListCursor(context: Context, uri: Uri): Cursor? {

        val documentFile = DocumentFile.fromTreeUri(context, uri)
        /*var fileUri: Uri? =
            null
        for (file in documentFile!!.listFiles()) {
            if (!file.isDirectory) {
                Log.d("CollectTracks", "Uri: " + file.uri.toString())
                fileUri = file.uri
            }
        }*/
        val contentResolver = context.contentResolver
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )

        val docCursor = contentResolver.query(
            docUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE),
            null,
            null,
            null
        )
        var folderName = ""

        if (docCursor != null) {
            while (docCursor.moveToNext()) {
                if (docCursor.getString(1) == "vnd.android.document/directory") {
                    Log.d(
                        "CollectTracks", "found directory = " + docCursor.getString(0) +
                                ", mime = " + docCursor.getString(1)
                    )
                    folderName = docCursor.getString(0)
                }
            }
        }
        docCursor?.close()

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID
        )

        return contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Audio.Media.DATA + " like ? ",
            arrayOf("%$folderName%"), null
        )
    }

    fun parseBitmap(childCursor: Cursor): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(childCursor.getString(0))
        val bmpBytes = try {
            mmr.embeddedPicture
        } catch (e: Exception) {
            Log.e("ParseBitmap", "Couldn't retrieve embedded picture")
            null
        }
        return if (bmpBytes != null) {
            BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.size)
        } else {
            null
        }
    }
}