package com.arvihealthscanner.Model

data class UpdateUserDetailResponse(
    val `data`: UpdateUserData,
    val success: Boolean
)

data class UpdateUserData(
    val updatedValues: UpdateUserValues
)

data class UpdateUserValues(
    val address: String,
    val fullName: String,
    val status: String,
    val userId: String,
    val uts: Long
)