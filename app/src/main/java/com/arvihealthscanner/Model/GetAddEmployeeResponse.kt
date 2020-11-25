package com.arvihealthscanner.Model

data class GetAddEmployeeResponse(
    val company: AddEmployeeCompany,
    val createdAt: String,
    val email: String,
    val employeeId: String,
    val id: Int,
    val isAdmin: Int,
    val isSuperAdmin: Int,
    val mobile: String,
    val name: String,
    val password: String,
    val permissions: Any,
    val picture: Any,
    val role: Any,
    val salt: String,
    val updatedAt: String
)

data class AddEmployeeCompany(
    val companyId: String,
    val createdAt: String,
    val faceDetection: AddEmployeeFaceDetection,
    val id: Int,
    val name: String,
    val status: Int,
    val updatedAt: String
)

data class AddEmployeeFaceDetection(
    val faceCollectionArn: String,
    val faceCollectionId: String,
    val faceCollectionStatus: Int,
    val faceModelVersion: String
)