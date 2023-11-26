package com.example.steganography

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.steganography.databinding.FragmentDecryptBinding
import java.io.ByteArrayOutputStream
import java.io.Console
import java.io.FileDescriptor
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DecryptFragment : Fragment() {
    private var _binding: FragmentDecryptBinding? = null
    private val binding get() =_binding!!
    lateinit var ivValue: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDecryptBinding.inflate(inflater, container, false)

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            val galleryUri = it
            try{
                val bitmap = uriToBitmap(galleryUri!!)
                binding.imageView.setImageBitmap(bitmap)
                binding.TextViewTextMultiLine.text = bitmap.toString()
            }catch(e:Exception){
                e.printStackTrace()
            }

        }

        binding.selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.decryptButton.setOnClickListener {
            val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("ivValue",
                Context.MODE_PRIVATE)
            val tempMessage : String? = sharedPreferences.getString("cipherText","default")
            //val tempiv : String? = sharedPreferences.getString("ivVal","default")
            val temp = binding.editTextTextMultiLine
            temp.setText(tempMessage)

                decryptMessage(tempMessage.toString().toByteArray())

            Toast.makeText(activity,tempMessage.toString(),Toast.LENGTH_SHORT).show()
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

    private fun gzip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(StandardCharsets.UTF_8).use { it.write(content) }
        return bos.toByteArray()
    }

    private fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

    private fun decryptMessage(dataToDecrypt:ByteArray) {
//        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("ivValue",
//            Context.MODE_PRIVATE)
//        val temp = sharedPreferences.getString("ivVal","default")
//        if (temp != "default") {
//            if (temp != null) {
//                ivValue = temp.toByteArray(Charsets.UTF_8)
//            }
//        }

        val myAct = activity as? MainActivity
        ivValue = myAct!!.returnIV()


        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey(binding.editTextKey.text.toString())
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivValue))
        val cipherText = cipher.doFinal(dataToDecrypt)
        //ivValue = cipher.iv
        //val decryptedMessage = buildString(cipherText, "decrypt")
        val decompressedMessage = ungzip(cipherText)

        Toast.makeText(activity,decompressedMessage, Toast.LENGTH_SHORT).show()
    }


    private fun generateKey(password: String): SecretKeySpec {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray()
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        val secretKeySpec = SecretKeySpec(key, "AES")
        return secretKeySpec
    }

    private fun buildString(text: ByteArray, status: String): String{
        val sb = StringBuilder()
        for (char in text) {
            sb.append(char.toInt().toChar())
        }
        return sb.toString()
    }


}