package com.unicorn.server.infrastructure.adapter.out.storage.exception

import com.unicorn.server.common.exception.BusinessException

class ObjectNotFoundException(objectKey: String) :
	BusinessException(ObjectStorageErrorCode.OBJECT_NOT_FOUND, "objectKey=$objectKey")
