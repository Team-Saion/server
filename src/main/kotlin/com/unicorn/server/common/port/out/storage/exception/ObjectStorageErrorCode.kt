package com.unicorn.server.common.port.out.storage.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ObjectStorageErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	INVALID_OBJECT_KEY("O400_1", "Invalid object key", HttpStatus.BAD_REQUEST),
	UNSUPPORTED_CONTENT_TYPE("O400_2", "Unsupported content type", HttpStatus.BAD_REQUEST),
	OBJECT_SIZE_EXCEEDED("O400_3", "Object size exceeds limit", HttpStatus.BAD_REQUEST),
	OBJECT_NOT_FOUND("O404_1", "Object not found", HttpStatus.NOT_FOUND),
	UPLOAD_FAILED("O500_1", "Object upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
	DELETE_FAILED("O500_2", "Object delete failed", HttpStatus.INTERNAL_SERVER_ERROR),
	URL_GENERATION_FAILED("O500_3", "Object URL generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
}
