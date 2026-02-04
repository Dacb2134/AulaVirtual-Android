package com.practicas.aulavirtualapp.network

data class PrivateFilesInfo(
    val filecount: Int,
    val filesize: Int,
    val folderCount: Int = 0
)