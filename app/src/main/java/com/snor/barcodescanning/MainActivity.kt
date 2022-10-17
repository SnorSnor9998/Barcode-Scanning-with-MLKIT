package com.snor.barcodescanning

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import com.snor.barcodescanning.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by viewBinding()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val barcode = it?.data?.getStringExtra("BarcodeResult")

//                val pic = it?.data?.getStringExtra("Image")
//                val image = B64Image.decode(pic.toString())

                binding.txtResult.text = barcode
//                binding.imgResult.setImageBitmap(image)

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.btnStart.setOnClickListener {
            val i = Intent(this, CamActivity::class.java)
            i.putExtra("title", "Example")
            i.putExtra("msg", "Scan Barcode")
            getContent.launch(i)
        }

    }

}