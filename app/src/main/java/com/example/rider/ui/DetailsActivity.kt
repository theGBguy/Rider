package com.example.rider.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.ActivityDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.arrivalholder.text = intent.getStringExtra("Arrival").toString()
        binding.departureholder.text = intent.getStringExtra("Departure").toString()
        binding.msgholder.text = intent.getStringExtra("Msg").toString()
        binding.arrivaltimeholder.text = intent.getStringExtra("ArrTime").toString()
        binding.arrdateholder.text = intent.getStringExtra("ArrDate").toString()
        binding.deptimeholder.text = intent.getStringExtra("DepTime").toString()
        binding.depdateholder.text = intent.getStringExtra("DepDate").toString()
        binding.weightholder.text = intent.getStringExtra("Weight").toString()
        binding.peopleholder.text = intent.getStringExtra("People").toString()
        binding.nameholder.text = intent.getStringExtra("Name").toString()

        binding.backBtnId.setOnClickListener { view: View? -> finish() }
        binding.acceptBtnId.setOnClickListener { view: View? ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Accept?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                    val intent = Intent(this@DetailsActivity, AcceptedRequestActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("No") { dialog: DialogInterface, id: Int -> dialog.cancel() }
                .show()
        }
    }
}