package com.banuba.sdk.example.beautification.gallery

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.banuba.sdk.example.beautification.utils.GlideImageLoader
import com.banuba.sdk.example.beautification.databinding.ItemGalleryBinding
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_gallery.view.*

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryItem>() {
    override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem.uri == newItem.uri &&
                oldItem.name == newItem.name
    }
}

class GalleryAdapter(
    private val imageLoader: GlideImageLoader,
    private val clickListener: ResourceListener
) : ListAdapter<GalleryItem, GalleryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val defaultPosition = -1
    private var activePosition = defaultPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, imageLoader)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position).resource, activePosition == position,
            ResourceListenerWrapper { resource ->
                activePosition = position
                clickListener.onClick(resource)
                notifyDataSetChanged()
            })
    }

    class ViewHolder(
        private val binding: ItemGalleryBinding,
        private val imageLoader: GlideImageLoader,
        override val containerView: View = binding.root) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        companion object {
            fun from(parent: ViewGroup, imageLoader: GlideImageLoader): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGalleryBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, imageLoader)
            }
        }

        fun bind(item: GalleryResource, selected: Boolean, listener: ResourceListenerWrapper) {
            binding.resource = item
            binding.clickListener = listener

            if (selected) {
                binding.thumbnailImageView.setColorFilter(Color.GRAY, PorterDuff.Mode.OVERLAY)
            } else {
                binding.thumbnailImageView.clearColorFilter()
            }

            imageLoader.loadThumbnail(
                view = containerView.thumbnailImageView,
                uri = item.fileUri
            )

            binding.executePendingBindings()
        }
    }
}

class ResourceListener(private val clickListener: (resource: GalleryResource) -> Unit) {
    fun onClick(resource: GalleryResource) = clickListener(resource)
}

class ResourceListenerWrapper(private val listener: (resource: GalleryResource) -> Unit) {
    fun onClick(resource: GalleryResource) = listener(resource)
}

data class GalleryItem(val resource: GalleryResource) {
    val uri = resource.fileUri
    val name = resource.fileName
}
