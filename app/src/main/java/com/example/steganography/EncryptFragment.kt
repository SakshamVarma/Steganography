package com.example.steganography

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.steganography.databinding.FragmentEncryptBinding
import java.io.FileDescriptor
import java.io.IOException

class EncryptFragment : Fragment() {
    private var _binding: FragmentEncryptBinding? = null
    private val binding get() =_binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEncryptBinding.inflate(inflater, container, false)

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            val galleryUri = it
            try{
                val bitmap = uriToBitmap(galleryUri!!)
                binding.imageView.setImageBitmap(bitmap)
            }catch(e:Exception){
                e.printStackTrace()
            }

        }

        binding.selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")

        }
        return binding.root
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor =
                context?.contentResolver?.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}