package com.example.rider.model

import com.google.type.LatLng

data class User(
    private val id: String = "-1",
    private val firstName: String,
    private val lastName: String,
    private val email: String,
    private val password: String,
    private val address: LatLng,
    private val type: Int
) {
    companion object {
        const val TYPE_STUDENT = 1
        const val TYPE_VOLUNTEER = 2
    }
}
