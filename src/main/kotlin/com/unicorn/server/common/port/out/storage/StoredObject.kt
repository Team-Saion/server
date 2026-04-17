package com.unicorn.server.common.port.out.storage

data class StoredObject(
	val objectKey: String,
	val url: String,
	val contentType: String,
	val contentLength: Long,
)
