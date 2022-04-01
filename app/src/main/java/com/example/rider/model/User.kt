package com.example.rider.model

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    var imageLocation: String = "",
    val type: Int = -1
) {
    companion object {
        const val TYPE_STUDENT = 1
        const val TYPE_VOLUNTEER = 2
    }
}
