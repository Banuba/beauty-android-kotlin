package com.banuba.sdk.example.beautification.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banuba.sdk.example.beautification.*
import com.banuba.sdk.example.beautification.databinding.FragmentCameraBinding
import com.rtugeek.android.colorseekbar.ColorSeekBar
import com.banuba.sdk.example.beautification.camera.Feature.LinearFeature
import com.banuba.sdk.example.beautification.camera.Feature.ColorFeature

class CameraFragment : Fragment() {

    companion object {
        private const val TAG = "CameraFragment"

        const val TEXTURE_PATH = "texturePath"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val viewModel by navGraphViewModels<CameraViewModel>(R.id.navigation)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        val binding: FragmentCameraBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_camera, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = FeaturesAdapter(FeatureListener {
            viewModel.onFeatureClicked(it)
        })
        binding.featuresList.adapter = adapter

        val manager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.featuresList.layoutManager = manager

        viewModel.activeFeatures.observe(viewLifecycleOwner, { features ->
            adapter.submitList(features.map { FeatureItem(it) }.toMutableList())
        })

        viewModel.currentFeature.observe(viewLifecycleOwner, { feature ->
            var view: View? = null
            val body = binding.featureBody
            when (feature) {
                is LinearFeature -> view = makeSeekBar(feature, body)
                is ColorFeature -> view = makeColorSeekBar(feature, body)
                else -> { /* nothing to do */ }
            }

            body.removeAllViews()
            view?.let {
                body.addView(view)
            }
        })

        viewModel.photoReady.observe(viewLifecycleOwner, {
            it?.let { image ->
                findNavController().navigate(
                    CameraFragmentDirections.actionCameraFragmentToPhotoFragment(image)
                )
                viewModel.photoProcessed()
            }
        })

        viewModel.textureClicked.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(
                    CameraFragmentDirections.actionCameraFragmentToGalleryFragment()
                )
                viewModel.selectTextureDone()
            }
        })

        viewModel.textureApplied.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().currentBackStackEntry?.savedStateHandle?.set(TEXTURE_PATH, "")
                viewModel.setTextureDone()
            }
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(TEXTURE_PATH)?.observe(
            viewLifecycleOwner) { path ->
            if (path.isNotEmpty()) {
                viewModel.setTexture(path)
            }
        }

        viewModel.attachSurface(binding.surfaceView)
        viewModel.restoreFeatures()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!allPermissionsGranted()) {
            val reqLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
                val notGranted = map.filter { !it.value }.map { it.key }.toList()
                if (notGranted.isNotEmpty()) {
                    for (permission in notGranted) {
                        Log.w(TAG, "Permission $permission was not granted")
                    }
                } else {
                    viewModel.openCamera()
                }
            }
            reqLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onStart() {
        super.onStart()

        if (allPermissionsGranted()) {
            viewModel.openCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.playbackPlay()
    }

    override fun onPause() {
        super.onPause()
        viewModel.playbackPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.releaseSurface()
        viewModel.closeCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireNotNull(activity).baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun makeSeekBar(feature: LinearFeature, root: ViewGroup?): View {
        val seekBar = layoutInflater.inflate(R.layout.linear_selector, root, false) as SeekBar
        val scale = 100
        seekBar.progress = (feature.value * scale).toInt()
        seekBar.max = (feature.maxValue * scale).toInt()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.onLinearFeatureChange(feature, progress.toFloat() / scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return seekBar
    }

    private fun makeColorSeekBar(feature: ColorFeature, root: ViewGroup?): View {
        val colorSeekBar = layoutInflater.inflate(R.layout.color_selector, root, false) as ColorSeekBar
        colorSeekBar.color = feature.color
        colorSeekBar.alphaBarPosition = feature.alphaPosition
        colorSeekBar.setOnColorChangeListener { _, alphaBarPosition, color ->
            viewModel.onColorFeatureChange(feature, color, alphaBarPosition)
        }

        return colorSeekBar
    }
}

