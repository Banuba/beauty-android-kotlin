package com.banuba.sdk.example.beautification.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions

class GlideImageLoader(context: Context? = null) {

    companion object {
        private const val THUMBNAIL_SIZE_MULTIPLIER = 0.01f
    }

    private val glideRequestManager = when (context) {
        is FragmentActivity -> Glide.with(context)
        is Activity -> Glide.with(context)
        else -> Glide.with(context!!)
    }

    fun loadThumbnail(
        view: ImageView,
        uri: Uri
    ) {
        val requestOptions = RequestOptions().apply {
            transform(CenterCrop())
        }
        glideRequestManager
            .load(uri)
            .apply(requestOptions)
            .thumbnail(THUMBNAIL_SIZE_MULTIPLIER)
            .into(view)
    }
}