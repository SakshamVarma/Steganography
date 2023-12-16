package com.example.steganography

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.example.steganography.databinding.FragmentDecryptBinding
import java.io.*
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                //binding.TextViewTextMultiLine.text = bitmap.toString()
            }catch(e:Exception){
                e.printStackTrace()
            }

        }

        binding.selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.decryptButton.setOnClickListener {

            val imageViewObj = binding.imageView.drawable
            if(imageViewObj != null){
                try {
                    val modifiedBitmap: Bitmap = binding.imageView.drawable.toBitmap()
                    val ansArr = decode(modifiedBitmap)
                    println("AnsARR : ${ansArr[0]}")
                    val baseStr = ansArr.toString(Charsets.UTF_8)
                    println("Base Str : $baseStr")
                    val decodedStr = Base64.decode(baseStr, Base64.DEFAULT)
                    val decryptStr = decryptMessage(decodedStr)
                    val decompressedStr = ungzip(decryptStr)


                    binding.TextViewAnswer.text = decompressedStr
                    binding.messageTitle.visibility = View.VISIBLE
                    binding.TextViewAnswer.visibility = View.VISIBLE



                    //Toast.makeText(activity, decompressedStr, Toast.LENGTH_SHORT).show()
                }
                catch (e:Exception)
                {
                    Toast.makeText(activity, "Invalid Image or PIN", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(activity,"Image not selected",Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        binding.messageTitle.visibility = View.INVISIBLE
        binding.TextViewAnswer.visibility = View.INVISIBLE
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

    private fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(StandardCharsets.UTF_8).use { it.readText()
        }

    private fun decryptMessage(dataToDecrypt:ByteArray):ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)
        val cipherText2 = ByteArray(dataToDecrypt.size - iv.size)
        System.arraycopy(dataToDecrypt, 0, iv, 0, iv.size)
        System.arraycopy(dataToDecrypt, iv.size, cipherText2, 0, cipherText2.size)
        val key = generateKey(binding.editTextKey.text.toString().trim())
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val cipherText = cipher.doFinal(cipherText2)
        return cipherText
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray()
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        val secretKeySpec = SecretKeySpec(key, "AES")
        return secretKeySpec
    }

    fun Int.toBinaryString(): String {
        return Integer.toBinaryString(this)
    }

    fun decode(modifiedBitmap: Bitmap):ByteArray {

        println("Extracting Size")
        var listSize = ""
        for (i in 0 until 8) {
            val pixel = modifiedBitmap.getPixel(i, 0)
            val blue = Color.blue(pixel)
            val binaryBlue = blue.toBinaryString().padStart(8, '0')
            listSize += binaryBlue[7]
        }
        println("Size in Int")
        val listSizeInt = listSize.toInt(2)
        val newSize = (listSize.toInt(2) + 1) * 8
        println(newSize)

        //println("Print Message")
        var x = 8
        var y = 0
        var i = 8
        var byteArrIndex = 0
        val byteArr = ByteArray(listSizeInt)

        while (y < modifiedBitmap.height && i < newSize) {
            while (x < modifiedBitmap.width && i < newSize) {

                var str = ""
                val k = x
                for (j in k until k + 8) {

                    val pixel = modifiedBitmap.getPixel(j, y)
                    val blue = Color.blue(pixel)
                    val binaryBlue = blue.toBinaryString().padStart(8, '0')
                    str += binaryBlue[7]

                    x = j
                    //x++
                    i++
                }
                x++

                //i++
                //println(str)
                byteArr[byteArrIndex++] = str.toInt(2).toByte()

            }
            y++
            x = 0
        }

        return byteArr

    }



}

