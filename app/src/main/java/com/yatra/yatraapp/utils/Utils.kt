package com.yatra.yatraapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import java.util.*

fun String.showShortToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun getRandomUID(): String {
    return UUID.randomUUID().toString().replace(Regex.fromLiteral("-"), "")
}

fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
    convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
    if (sourceDrawable == null) {
        return null
    }
    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
        val constantState = sourceDrawable.constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}