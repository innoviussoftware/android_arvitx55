package com.arvihealthscanner.Model

data class DetectFaceNewResponse(
    val `data`: DetectFaceNewData
)

data class DetectFaceNewData(
    val createdAt: String,
    val email: String,
    val employeeId: String,
    val id: Int,
    val isAdmin: Int,
    val isSuperAdmin: Int,
    val mobile: String,
    val name: String,
    val password: Any,
    val permissions: Any,
    val picture: String,
    val salt: Any,
    val updatedAt: String
)