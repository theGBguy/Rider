package com.example.rider.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.databinding.ActivityAboutOptionMenuBinding

class AboutOptionMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityAboutOptionMenuBinding.inflate(layoutInflater).root)
    }
}