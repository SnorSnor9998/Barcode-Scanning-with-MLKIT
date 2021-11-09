package com.snor.barcodescanning

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import com.snor.barcodescanning.databinding.ActivityMainBinding

private const val REQUEST_CODE = 88

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.btnStart.setOnClickListener {
            scanning()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val barcode = data?.getStringExtra("BarcodeResult")
            binding.txtResult.text = barcode
        }
    }


    private fun scanning(){
        val i = Intent(this, CamActivity::class.java)
        i.putExtra("title", "Example")
        i.putExtra("msg", "Scan QR code to proceed")
        startActivityForResult(i, REQUEST_CODE)

    }


}