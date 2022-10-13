package com.snor.barcodescanning

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.graphics.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeAnalyzer(
    private val barcodeListener: (barcode: String, bm: Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {

            val height = mediaImage.height
            val width = mediaImage.width

            //Since in the end the image will rotate clockwise 90 degree
            //left -> top, top -> right, right -> bottom, bottom -> left

            //Top    : (far) -value > 0 > +value (closer)
            val c1x = (width * 0.125).toInt() + 150
            //Right  : (far) -value > 0 > +value (closer)
            val c1y = (height * 0.25).toInt() - 25
            //Bottom : (closer) -value > 0 > +value (far)
            val c2x = (width * 0.875).toInt() - 150
            //Left   : (closer) -value > 0 > +value (far)
            val c2y = (height * 0.75).toInt() + 25

            val rect = Rect(c1x, c1y, c2x, c2y)

            val ori: Bitmap = imageProxy.toBitmap()!!
            val crop = Bitmap.createBitmap(ori, rect.left, rect.top, rect.width(), rect.height())
            val rImage = crop.rotate(90F)

            val image: InputImage =
                InputImage.fromBitmap(rImage, imageProxy.imageInfo.rotationDegrees)

            // Pass image to the scanner and have it do its thing
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    for (barcode in barcodes) {
                        barcodeListener(barcode.rawValue ?: "", image.bitmapInternal!!)
                        imageProxy.close()
                    }
                }
                .addOnFailureListener {
                    // You should really do something about Exceptions
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    // It's important to close the imageProxy
                    imageProxy.close()
                }
        }
    }
}