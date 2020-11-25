package com.arvihealthscanner.Model

data class GetLoginResponse(
    val accessToken: String,
    val user: GetLoginUser
)

data class GetLoginUser(
    val createdAt: String,
    val email: String,
    val employeeId: String,
    val id: Int,
    val isAdmin: Int,
    val isSuperAdmin: Int,
    val mobile: String,
    val name: Any,
    val password: String,
    val permissions: Any,
    val picture: String,
    val role: String,
    val salt: String,
    val updatedAt: String
)