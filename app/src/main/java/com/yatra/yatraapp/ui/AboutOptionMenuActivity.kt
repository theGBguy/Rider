package com.yatra.yatraapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yatra.yatraapp.databinding.ActivityAboutOptionMenuBinding

class AboutOptionMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityAboutOptionMenuBinding.inflate(layoutInflater).root)
    }
}