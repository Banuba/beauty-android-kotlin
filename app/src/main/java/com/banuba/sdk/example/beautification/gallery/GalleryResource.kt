package com.banuba.sdk.example.beautification.gallery

import android.net.Uri

sealed class GalleryResource {
    abstract val fileUri: Uri
    abstract val fileName: String
}

data class Empty(
    override val fileUri: Uri = Uri.EMPTY,
    override val fileName: String = ""
) : GalleryResource()

data class Picture(
    override val fileUri: Uri,
    override val fileName: String
) : GalleryResource()

data class Video(
    override val fileUri: Uri,
    override val fileName: String,
    val duration: Long
) : GalleryResource()
