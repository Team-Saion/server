package com.unicorn.server.common.port.out.storage.exception

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.storage.ObjectType

// UnsupportedContentTypeException - 업로드된 파일의 contentType이 ObjectType에서 허용하지 않을 때 사용한다.
class UnsupportedContentTypeException(contentType: String, objectType: ObjectType) :
	BusinessException(
		ObjectStorageErrorCode.UNSUPPORTED_CONTENT_TYPE,
		"contentType=$contentType, objectType=$objectType, allowed=${objectType.allowedContentTypes}",
	)
