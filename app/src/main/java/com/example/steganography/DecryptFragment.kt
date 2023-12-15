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
                binding.TextViewTextMultiLine.text = bitmap.toString()
            }catch(e:Exception){
                e.printStackTrace()
            }

        }

        binding.selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.decryptButton.setOnClickListener {
//            val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("ivValue",
//                Context.MODE_PRIVATE)
//            val tempMessage1 : String? = sharedPreferences.getString("cipherText","default")
//
//            val myAct = activity as? MainActivity
//            val tempMessage = myAct!!.returnEncData()
//
//
//            println("from with conversion : ${tempMessage1!!.toByteArray(Charsets.UTF_8)}")
//            println("from shared pref : ${tempMessage1}")
//            println("from without conversion : ${tempMessage}")


            //val tempiv : String? = sharedPreferences.getString("ivVal","default")

            //val temp = binding.editTextTextMultiLine
            //temp.setText(tempMessage.toString())

                //decryptMessage(tempMessage)

            val imageViewObj = binding.imageView.drawable
            if(imageViewObj != null){
                val modifiedBitmap: Bitmap = binding.imageView.drawable.toBitmap()
                val ansArr = decode(modifiedBitmap)

                Toast.makeText(activity,ByteArrayToString(ansArr),Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(activity,"Image not selected",Toast.LENGTH_SHORT).show()
            }

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


    private fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

    private fun decryptMessage(dataToDecrypt:ByteArray) {

        val myAct = activity as? MainActivity
        ivValue = myAct!!.returnIV()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey(binding.editTextKey.text.toString().trim())
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

//    private fun buildString(text: ByteArray, status: String): String{
//        val sb = StringBuilder()
//        for (char in text) {
//            sb.append(char.toInt().toChar())
//        }
//        return sb.toString()
//    }

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


    private fun saveBitmapImage(bitmap: Bitmap) {
        val timestamp = System.currentTimeMillis()

        //Tell the media scanner about the new file so that it is immediately available to the user.
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name))
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireActivity().contentResolver.update(uri, values, null, null)

                    Toast.makeText(activity, "Saved...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
                }
            }
        } else {
            val imageFileFolder = File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name))
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$timestamp.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                Toast.makeText(activity, "Saved...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
            }
        }
    }

    fun ByteArrayToString(byteArr:ByteArray): String{
        return String(byteArr)
    }



}

