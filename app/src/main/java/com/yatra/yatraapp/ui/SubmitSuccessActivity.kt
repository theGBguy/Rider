package com.yatra.yatraapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yatra.yatraapp.databinding.ActivitySubmitSuccessBinding

class SubmitSuccessActivity : AppCompatActivity() {
    private var binding: ActivitySubmitSuccessBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubmitSuccessBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding?.submitHomeBtnId?.setOnClickListener { _ ->
//            Intent(this@SubmitSuccessActivity, StudentSideNavBarActivity::class.java).also {
//                startActivity(it)
//            }
            finish()
        }
    }
}