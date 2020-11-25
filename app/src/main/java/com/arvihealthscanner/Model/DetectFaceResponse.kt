package com.arvihealthscanner.Model

data class DetectFaceResponse(
    val address: String,
    val companyId: String,
    val fullName: String,
    val mobile: String,
    val status: String,
    val ts: Int,
    val userId: String,
    val uts: Long
)