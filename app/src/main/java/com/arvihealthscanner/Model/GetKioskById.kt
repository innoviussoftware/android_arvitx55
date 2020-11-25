package com.arvihealthscanner.Model

data class GetKioskById(
    val company: GetKioskCompany,
    val companyId: String,
    val deviceId: String,
    val imei: String,
    val kioskId: String,
    val kioskLocation: String,
    val ts: Int
)

data class GetKioskCompany(
    val companyId: String,
    val companyName: String,
    val faceCollectionArn: String,
    val faceCollectionId: String,
    val faceCollectionStatus: Int,
    val faceModelVersion: String,
    val maxUsers: String,
    val trialEnddate: String,
    val uts: Long
)