package com.unicorn.server.infrastructure.adapter.out.redis.exception

import com.unicorn.server.common.exception.BusinessException

class RedisKeyNotFoundException(key: String) :
	BusinessException(RedisErrorCode.REDIS_KEY_NOT_FOUND, "key=$key")
