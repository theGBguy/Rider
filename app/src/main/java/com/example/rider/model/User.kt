package com.example.rider.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var profileImageLocation: String = "",
    var idImageLocation: String = "",
    val type: Int = -1
) : Parcelable {
    companion object {
        const val TYPE_STUDENT = 1
        const val TYPE_VOLUNTEER = 2
    }
}
