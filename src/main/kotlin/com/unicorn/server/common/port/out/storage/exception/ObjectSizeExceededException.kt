package com.unicorn.server.common.port.out.storage.exception

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.storage.ObjectType

// ObjectSizeExceededException - 업로드된 파일 용량이 ObjectType의 최대 용량을 초과할 때 사용한다.
class ObjectSizeExceededException(contentLength: Long, objectType: ObjectType) :
	BusinessException(
		ObjectStorageErrorCode.OBJECT_SIZE_EXCEEDED,
		"contentLength=$contentLength, maxSizeBytes=${objectType.maxSizeBytes}, objectType=$objectType",
	)
