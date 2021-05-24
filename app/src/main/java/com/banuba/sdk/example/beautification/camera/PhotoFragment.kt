package com.banuba.sdk.example.beautification.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.banuba.sdk.example.beautification.R
import com.banuba.sdk.example.beautification.databinding.FragmentPhotoBinding

class PhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentPhotoBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_photo, container, false
        )

        val args = PhotoFragmentArgs.fromBundle(requireArguments())
        binding.imageView.setImageBitmap(args.imageBitmap)

        return binding.root
    }
}