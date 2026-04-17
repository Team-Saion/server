package com.unicorn.server.common.port.out.storage

import java.io.InputStream

/**
 * 데이터 업로드할 때 정보를 담는 클래스
 */
data class ObjectUploadCommand(
	val objectKey: String,
	val contentType: String,
	val contentLength: Long,
	val inputStream: InputStream,
)
