package com.arvihealthscanner.Model

data class UploadPhotoResponse(
    val `data`: List<UploadPhotoData>
)

data class UploadPhotoData(
    val filename: String,
    val mimetype: String,
    val path: String
)