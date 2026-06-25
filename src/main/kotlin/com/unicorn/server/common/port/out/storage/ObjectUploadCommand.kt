package com.unicorn.server.common.port.out.storage

import java.io.InputStream

/**
 * 데이터 업로드할 때 정보를 담는 클래스.
 *
 * objectKey/contentType은 ObjectType.validate()/generateObjectKey()를 통과한 값이어야 한다.
 * 이 클래스 자체는 그 검증을 다시 수행하지 않는다.
 */
data class ObjectUploadCommand(
	val objectKey: String,
	val contentType: String,
	val contentLength: Long,
	val inputStream: InputStream,
)
