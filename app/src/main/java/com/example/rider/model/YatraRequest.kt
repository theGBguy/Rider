package com.example.rider.model

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class YatraRequest(
    val requestId: String? = "",
    val initiatorId: String? = "",
    val acceptorId: String? = "",
    val arrivalLocation: String? = "",
    val arrivalLatitude: Double? = 0.0,
    val arrivalLongitude: Double? = 0.0,
    val arrivalDate: String? = "",
    val arrivalTime: String? = "",
    val departureLocation: String? = "",
    val departureLatitude: Double? = 0.0,
    val departureLongitude: Double? = 0.0,
    val departureDate: String? = "",
    val departureTime: String? = "",
    val peopleCount: Int? = -1,
    val weight: Int? = -1,
    val msg: String? = "",
    val completed: Boolean? = false
) : Parcelable