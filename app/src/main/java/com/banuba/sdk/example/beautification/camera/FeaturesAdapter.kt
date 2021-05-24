package com.banuba.sdk.example.beautification.camera

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.banuba.sdk.example.beautification.databinding.FeatureBinding

class FeaturesAdapter(private val clickListener: FeatureListener) : ListAdapter<FeatureItem, RecyclerView.ViewHolder>(
    FeaturesDiffCallback()
) {

    private val defaultPosition = -1
    private var activePosition = defaultPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TextViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> {
                val featureItem = getItem(position)
                holder.bind(featureItem.feature, activePosition == position,
                    FeatureListenerWrapper { feature: Feature ->
                        activePosition = position
                        clickListener.onClick(feature)
                        notifyDataSetChanged()
                })
            }
        }
    }

    override fun submitList(list: MutableList<FeatureItem>?) {
        super.submitList(list)

        activePosition = defaultPosition
    }

    class TextViewHolder(private val binding: FeatureBinding): RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FeatureBinding.inflate(layoutInflater, parent, false)
                return TextViewHolder(binding)
            }
        }

        fun bind(item: Feature, selected: Boolean, listener: FeatureListenerWrapper) {
            binding.feature = item
            binding.clickListener = listener
            binding.selected = selected
            binding.executePendingBindings()
        }
    }
}

class FeaturesDiffCallback : DiffUtil.ItemCallback<FeatureItem>() {
    override fun areItemsTheSame(oldItem: FeatureItem, newItem: FeatureItem): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: FeatureItem, newItem: FeatureItem): Boolean {
        return oldItem == newItem
    }
}

class FeatureListener(private val clickListener: (name: String) -> Unit) {
    fun onClick(feature: Feature) = clickListener(feature.name)
}

class FeatureListenerWrapper(private val listener: (feature: Feature) -> Unit) {
    fun onClick(feature: Feature) = listener(feature)
}

data class FeatureItem(val feature: Feature) {
    val name = feature.name
}
