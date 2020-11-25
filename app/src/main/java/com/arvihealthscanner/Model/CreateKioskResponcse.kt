package com.arvihealthscanner.Model

data class CreateKioskResponcse(
    val companyId: String,
    val deviceId: String,
    val imei: String,
    val kioskId: String,
    val kioskLocation: String
)