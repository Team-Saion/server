package com.unicorn.server.common.port.out.storage

import com.unicorn.server.common.port.out.storage.exception.ObjectSizeExceededException
import com.unicorn.server.common.port.out.storage.exception.UnsupportedContentTypeException
import java.util.UUID

/**
 * 업로드 가능한 파일 유형과 유형별 정책(허용 contentType, 최대 용량, 저장 경로 prefix)을 표현하는 값 객체.
 *
 * ObjectStorage/어댑터(S3 등)는 이 타입을 모른다. 업로드를 요청하는 use-case가 ObjectUploadCommand를
 * 만들기 전에 validate()로 검증하고 generateObjectKey()로 objectKey를 만들어야 한다.
 * 새 파일 유형이 필요하면 이 enum에 상수만 추가하면 되고, ObjectStorage 쪽은 수정할 필요가 없다.
 */
enum class ObjectType(
	val allowedContentTypes: Set<String>,
	val maxSizeBytes: Long,
	val keyPrefix: String,
) {
	PROFILE_IMAGE(
		allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp"),
		maxSizeBytes = 20 * 1024 * 1024, // MB
		keyPrefix = "images/profile",
	),
	HTML_DOCUMENT(
		allowedContentTypes = setOf("text/html"),
		maxSizeBytes = 1 * 1024 * 1024, // MB
		keyPrefix = "documents/html",
	),
	;

	// 허용된 contentType/최대 용량을 벗어나면 예외를 던진다. S3 등 실제 업로드 호출 전에 실행되어야 한다.
	fun validate(contentType: String, contentLength: Long) {
		require(contentLength >= 0) { "Content length must be non-negative" }
		if (contentType !in allowedContentTypes) throw UnsupportedContentTypeException(contentType, this)
		if (contentLength > maxSizeBytes) throw ObjectSizeExceededException(contentLength, this)
	}

	// keyPrefix + UUID + 원본 파일 확장자를 조합해 충돌 없는 objectKey를 만든다.
	fun generateObjectKey(originalFilename: String): String {
		val extension = originalFilename.substringAfterLast('.', missingDelimiterValue = "")
			.filter { it.isLetterOrDigit() }
		val suffix = if (extension.isBlank()) "" else ".$extension"
		return "$keyPrefix/${UUID.randomUUID()}$suffix"
	}
}
