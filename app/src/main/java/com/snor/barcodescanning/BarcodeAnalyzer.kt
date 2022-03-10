package com.snor.barcodescanning

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.graphics.*

class BarcodeAnalyzer(private val barcodeListener: (barcode: String) -> Unit) :
    ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && mediaImage.format == ImageFormat.YUV_420_888) {

            val height = mediaImage.height
            val width = mediaImage.width

            //Coordinate 1
            val c1x = (width * 0.125).toInt() + 150 //left
            val c1y = (height * 0.25).toInt() //top

            //Coordinate 2
            val c2x = (width * 0.875).toInt() - 150 //right
            val c2y = (height * 0.75).toInt() //bottom

            val rect = Rect(c1x, c1y, c2x, c2y)
//            Log.d("dd--", "Rectangle: $rect")

            val ori: Bitmap = imageProxy.toBitmap()!!
            val crop = Bitmap.createBitmap(ori, rect.left, rect.top, rect.width(), rect.height())

            val image: InputImage =
                InputImage.fromBitmap(crop, imageProxy.imageInfo.rotationDegrees)

            // Pass image to the scanner and have it do its thing
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    for (barcode in barcodes) {
                        barcodeListener(barcode.rawValue ?: "")
                    }
                }
                .addOnFailureListener {
                    // You should really do something about Exceptions
                }
                .addOnCompleteListener {
                    // It's important to close the imageProxy
                    imageProxy.close()
                }
        }
    }
}