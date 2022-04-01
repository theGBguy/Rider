package com.example.rider.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat

fun String.showShortToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun EditText.setOnConsistentClickListener(doOnClick: (View) -> Unit) {
    val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(event: MotionEvent?): Boolean {
                doOnClick(this@setOnConsistentClickListener)
                return false
            }
        })

    this.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
}