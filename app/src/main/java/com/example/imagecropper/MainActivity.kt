package com.example.imagecropper

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions



class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var cropButton: Button
    private val viewModel: OCRViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        cropButton = findViewById(R.id.cropButton)

        val croppedImage = cropImageFromPosition()
        if (croppedImage != null) {
            imageView.setImageBitmap(croppedImage)
            if (isInternetAvailable()) {
                viewModel.extractTextFromImage(croppedImage)
            } else {
                Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show()
            }


            //MLKIT
            extractTextFromImageMLKit(croppedImage) {
                val ocr_result = it.replace(" ", "")
                Log.d("OCR RESULT", ocr_result)
            }
        }

        viewModel.textResult.observe(this) { result ->
            result?.let { ocrResult ->
                Log.d("OCR RESULT API", ocrResult.replace(" ", ""))
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cropImageFromPosition(): Bitmap? {
        val imageViewBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
        if (imageViewBitmap != null) {
            val left = 0
            val top = imageViewBitmap.height - 35 // crop 40px from bottom
            val width = 400 //cropped image width
            val height = 150 //cropped image height

            val cropLeft = maxOf(0, left)
            val cropTop = maxOf(0, top)
            val cropWidth = minOf(imageViewBitmap.width - cropLeft, width)
            val cropHeight = minOf(imageViewBitmap.height - cropTop, height)

            if (cropWidth > 0 && cropHeight > 0) {
                return Bitmap.createBitmap(
                    imageViewBitmap,
                    cropLeft,
                    cropTop,
                    cropWidth,
                    cropHeight
                )
            }
        }
        return null
    }

    //using mlkit google
    private fun extractTextFromImageMLKit(image: Bitmap, callback: (String) -> Unit) {
        val inputImage = InputImage.fromBitmap(image, 0)
        val options = TextRecognizerOptions.Builder()
            .build()
        val textRecognizer = TextRecognition.getClient(options)
        val result = textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                callback(extractedText)
            }
            .addOnFailureListener { exception ->
                callback("")
            }
    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}











