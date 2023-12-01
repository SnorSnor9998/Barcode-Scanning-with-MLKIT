package com.snor.barcodescanning

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.snor.barcodescanning.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val barcode = it?.data?.getStringExtra("BarcodeResult")
                binding.txtResult.text = barcode
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.txtResult.setOnLongClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Copied Text", binding.txtResult.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        binding.btnSingle.setOnClickListener {
            val i = Intent(this, CamActivity::class.java)
            i.putExtra("title", "Example")
            i.putExtra("msg", "Scan Barcode")
            getContent.launch(i)
        }

        binding.btnContinuous.setOnClickListener {
            val i = Intent(this, ContinuousCamActivity::class.java)
            i.putExtra("title", "Example")
            i.putExtra("msg", "Scan Barcode")
            getContent.launch(i)
        }

    }

}