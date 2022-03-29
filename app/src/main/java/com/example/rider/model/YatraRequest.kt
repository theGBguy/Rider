package com.example.rider.model

data class YatraRequest(
    val requestId: String?,
    val initiatorId: String?,
    val acceptorId: String?,
    val arrival: String?,
    val arrivalDate: String?,
    val arrivalTime: String?,
    val departure: String?,
    val departureDate: String?,
    val departureTime: String?,
    val peopleCount: Int?,
    val weight: String?,
    val name: String?,
    val msg: String?,
    val isAccepted: Boolean?
)