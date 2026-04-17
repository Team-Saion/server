package com.unicorn.server.infrastructure.adapter.out.redis.exception

import com.unicorn.server.common.exception.BusinessException

class RedisOperationFailedException(
	operation: String,
	key: String,
	cause: Throwable? = null,
) : BusinessException(RedisErrorCode.REDIS_OPERATION_FAILED, "operation=$operation, key=$key", cause)
