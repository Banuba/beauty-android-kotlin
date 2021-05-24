package com.banuba.sdk.example.beautification.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.banuba.sdk.example.beautification.utils.GlideImageLoader
import com.banuba.sdk.example.beautification.R
import com.banuba.sdk.example.beautification.camera.CameraFragment
import com.banuba.sdk.example.beautification.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var selected: GalleryResource? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentGalleryBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_gallery, container, false
        )

        val galleryViewModelFactory = GalleryViewModelFactory(requireNotNull(activity).application)
        val galleryViewModel = ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = GalleryAdapter(GlideImageLoader(context), ResourceListener { resource ->
            Log.i("[===png]", "path: ${resource.fileUri.toFile().path}")
            binding.resourceName.text = resource.fileName

            selected = resource
        })
        binding.galleryList.adapter = adapter
        binding.galleryList.layoutManager = GridLayoutManager(context, 3)

        galleryViewModel.galleryResources.observe(viewLifecycleOwner, { resources ->
            val galleryItems = resources.map { GalleryItem(it) }
            adapter.submitList(galleryItems)
        })

        binding.select.setOnClickListener {
            selected?.let {
                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set(CameraFragment.TEXTURE_PATH, it.fileUri.toFile().path)
                navController.popBackStack()
            }
        }

        return binding.root
    }
}