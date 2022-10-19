package com.snor.barcodescanning

import android.Manifest
import android.content.Intent
import android.hardware.camera2.CaptureRequest
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.*
import android.view.Surface
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_90
import android.view.View
import android.viewbinding.library.activity.viewBinding
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.snor.barcodescanning.databinding.ActivityCamBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class CamActivity : AppCompatActivity() {

    private val binding: ActivityCamBinding by viewBinding()

    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    // Select back camera
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cam)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.launch(Manifest.permission.CAMERA)

        //set title and message
        binding.lblTitle.text = intent.extras?.getString("title")
        binding.lblSubTitle.text = intent.extras?.getString("msg")

        //Back Button
        binding.btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            super.onBackPressed()
        }

        //Flash Button
        binding.btnFlash.setOnClickListener {
            flash()
        }
    }

    override fun onResume() {
        super.onResume()
        processingBarcode.set(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        try {
            // Create an instance of the ProcessCameraProvider,
            // which will be used to bind the use cases to a lifecycle owner.
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            // Add a listener to the cameraProviderFuture.
            // The first argument is a Runnable, which will be where the magic actually happens.
            // The second argument (way down below) is an Executor that runs on the main thread.
            cameraProviderFuture.addListener({

                cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build()
                binding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)

                // Setup the ImageAnalyzer for the ImageAnalysis use case
                val builder = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1080, 1920))

                imageAnalysis = builder.build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                            if (processingBarcode.compareAndSet(false, true)) {
                                beep()
                                Log.d("dd--", "Result: $barcode")

                                val intent = Intent()
                                intent.putExtra("BarcodeResult", barcode)
                                setResult(RESULT_OK, intent)
                                finish()

                            }
                        })
                    }

                try {
                    // Unbind any bound use cases before rebinding
                    cameraProvider.unbindAll()
                    // Bind use cases to lifecycleOwner
                    val cam =
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                    if (cam.cameraInfo.hasFlashUnit()) {
                        cam.cameraControl.enableTorch(flashOn)
                        binding.btnFlash.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    Log.e("PreviewUseCase", "Binding failed! :(", e)
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun flash() {
        flashOn = !flashOn
        // Change icon
        val id = if (flashOn) R.drawable.ic_flash_off else R.drawable.ic_flash_on
        binding.btnFlash.setImageDrawable(ContextCompat.getDrawable(this, id))

        try {
            // Bind use cases to lifecycleOwner
            val cam =
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            cam.cameraControl.enableTorch(flashOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun beep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }

}
