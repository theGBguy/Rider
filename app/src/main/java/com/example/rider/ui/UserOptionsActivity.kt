package com.example.rider.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.ActivityUserOptionsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityUserOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStudent.setOnClickListener {
            val intent = Intent(this@UserOptionsActivity, StudentLoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnVolunteer.setOnClickListener {
            val intent = Intent(this@UserOptionsActivity, VolunteerLoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setIcon(R.mipmap.ic_launcher)
            .setMessage("Do you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> finish() }
            .setNegativeButton("No") { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .show()
    }
}