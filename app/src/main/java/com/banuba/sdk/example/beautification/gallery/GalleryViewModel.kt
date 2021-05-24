package com.banuba.sdk.example.beautification.gallery

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val DATE_TAKEN = MediaStore.MediaColumns.DATE_TAKEN
        private const val DATA = MediaStore.Files.FileColumns.DATA
        private const val MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE
        val SIMPLE_MEDIA_DATA_PROJECTION = arrayOf(
            DATA,
            MEDIA_TYPE
        )
        const val SORT_ORDER = "$DATE_TAKEN DESC"
    }

    private val _galleryResources: MutableLiveData<List<GalleryResource>> by lazy {
        val liveData = MutableLiveData<List<GalleryResource>>()

        val selection = MediaStore.MediaColumns.MIME_TYPE + "='image/png'"
        val cursor = getApplication<Application>().applicationContext.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            SIMPLE_MEDIA_DATA_PROJECTION,
            selection,
            null,
            SORT_ORDER
        )
        val resultList = cursor.extractDataList { dataCursor ->
            dataCursor.toSimpleGalleryResource()
        }

        cursor?.close()

        liveData.value = resultList
        return@lazy liveData
    }
    val galleryResources: LiveData<List<GalleryResource>>
        get() = _galleryResources

    private fun Cursor.toSimpleGalleryResource(): GalleryResource {
        val mediaType = getInt(getColumnIndex(MEDIA_TYPE))
        val path = "file://${getString(getColumnIndex(DATA))}"
        val fileUri: Uri = Uri.parse(path)
        val fileName: String = fileUri.lastPathSegment ?: ""

        return if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            Picture(
                fileUri = fileUri,
                fileName = fileName
            )
        } else {
            Video(
                fileUri = fileUri,
                fileName = fileName,
                duration = 0L
            )
        }
    }

    private fun <T : Any> Cursor?.extractDataList(
        mapFunction: (Cursor) -> T?
    ): List<T> {
        return this?.let {
            generateSequence {
                if (it.moveToNext()) it else null
            }.mapNotNull { cursor ->
                mapFunction(cursor)
            }.toList()
        } ?: emptyList()
    }
}