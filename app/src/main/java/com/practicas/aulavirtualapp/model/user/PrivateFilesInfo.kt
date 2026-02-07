package com.practicas.aulavirtualapp.model.user

data class PrivateFilesInfo(
    val filecount: Int,
    val filesize: Int,
    val folderCount: Int = 0
)