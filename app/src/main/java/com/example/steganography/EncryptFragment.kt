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
import androidx.core.graphics.drawable.toBitmap
import com.example.steganography.databinding.FragmentEncryptBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.*
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptFragment : Fragment() {
    private var _binding: FragmentEncryptBinding? = null
    private val binding get() =_binding!!
    lateinit var ivValue: ByteArray
    private var db = Firebase.firestore

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
                val inputBitmap = uriToBitmap(galleryUri!!)
                binding.imageView.setImageBitmap(inputBitmap)

            }catch(e:Exception){
                e.printStackTrace()
            }

        }

        binding.selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")

        }


        binding.encryptButton.setOnClickListener {
            val imageViewObj = binding.imageView.drawable
            if(imageViewObj != null)
            {
                val bitmapOriginal: Bitmap = binding.imageView.drawable.toBitmap()
                encryptMessage(bitmapOriginal)
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

    private fun gzip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
        return bos.toByteArray()
    }

    private fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }

    private fun encryptMessage(bitmapOriginal:Bitmap) {
        val message = binding.editTextTextMultiLine.text.toString().trim()
        val compressMessage = gzip(message)
        val plainText = compressMessage

//        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("ivValue",
//            Context.MODE_PRIVATE)
//        val edit : SharedPreferences.Editor = sharedPreferences.edit()

        val key = generateKey(binding.editTextKey.text.toString())

        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key,ivSpec)
        val cipherText = cipher.doFinal(plainText)

        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        ivValue = cipher.iv
        println("IV Value : $ivValue")


//        edit.putString("ivVal",ivValue.toString())
//        edit.putString("cipherText",cipherText.decodeToString())
//        edit.apply()

//        val myAct = activity as? MainActivity
//        myAct!!.updateIV(ivValue)
//        myAct.updateEncData(cipherText)

        val bitmap = Bitmap.createScaledBitmap(bitmapOriginal, 640, 480, true)
        println("CipherText Combined $combined")


        //val compressedMessage = gzip(message)
        //val modifiedBitmap = embed(bitmap, compressedMessage)

        val modifiedBitmap = embed(bitmap, combined)

        saveBitmapImage(modifiedBitmap)
        storeMessage(message)

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

    fun Int.toBinaryString(): String {
        return Integer.toBinaryString(this)
    }

    fun embed(originalBitmap: Bitmap, byteArr: ByteArray): Bitmap {
        val modifiedBitmap = originalBitmap.copy(originalBitmap.config, true)

        val byteStr = Base64.encodeToString(byteArr, Base64.DEFAULT).padStart(8,'0')
        println("byteStr : $byteStr")
        val byteStrByteArr = byteStr.toByteArray()

        val messageSize = byteStrByteArr.size

        val sizeBin = messageSize.toString(2).padStart(8, '0')
        println("Size in string " + messageSize.toString(2).padStart(8, '0'))

        for (x in 0 until 8) {
            val pixel = modifiedBitmap.getPixel(x, 0)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            val binaryBlue = blue.toBinaryString().padStart(8, '0')
            val temp = binaryBlue.substring(0, 7) + sizeBin[x]
            val modBlueInt = temp.toInt(2)

            modifiedBitmap.setPixel(x, 0, Color.rgb(red, green, modBlueInt))

        }
        var x = 8
        var y = 0
        var byteArrIndex = 0
        //println("Byte Array : $byteArr")


        while (y < modifiedBitmap.height && byteArrIndex < byteStrByteArr.size) {
            while (x < modifiedBitmap.width && byteArrIndex < byteStrByteArr.size) {
                val temp = byteStrByteArr[byteArrIndex].toInt().toString(2).padStart(8, '0')

                for (i in 0 until temp.length) {
                    val pixel = modifiedBitmap.getPixel(x, y)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)
                    val binaryBlue = blue.toBinaryString().padStart(8, '0')

                    //println("-----Temp : ${temp}")
                    //println("-----Temp i : ${temp[i]}")

                    val temp2 = binaryBlue.substring(0, 7) + temp[i]
                    val modBlueInt = temp2.toInt(2)

                    modifiedBitmap.setPixel(x, y, Color.rgb(red, green, modBlueInt))

                    x++
                }

                byteArrIndex++
            }
            y++
            x = 0
        }

        return modifiedBitmap
    }


    fun ByteArrayToString(byteArr:ByteArray): String{
        return String(byteArr)
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

    fun storeMessage(input:String)
    {

        val userMap = hashMapOf(
            "message" to input
        )

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        //val count = db.collection(userId).count()
        //count++
        db.collection(userId).add(userMap)
            .addOnSuccessListener {
                Toast.makeText(activity,"Successfully Added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity,"Upload Failed", Toast.LENGTH_SHORT).show()
            }
    }



}