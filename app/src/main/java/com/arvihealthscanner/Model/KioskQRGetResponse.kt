package com.arvihealthscanner.Model

data class KioskQRGetResponse(
    val kioskId: String,
    val qrCode: String,
    val recordId: Int
)