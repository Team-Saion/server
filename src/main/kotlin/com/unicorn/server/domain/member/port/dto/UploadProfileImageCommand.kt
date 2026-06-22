package com.unicorn.server.domain.member.port.dto

import java.io.InputStream

data class UploadProfileImageCommand(
	val originalFilename: String,
	val contentType: String,
	val contentLength: Long,
	val inputStream: InputStream,
)
