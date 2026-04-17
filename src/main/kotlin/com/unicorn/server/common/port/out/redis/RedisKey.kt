package com.unicorn.server.common.port.out.redis

@JvmInline
value class RedisKey(val value: String) {
	init {
		require(value.isNotBlank()) { "Redis key cannot be blank" }
	}

	override fun toString(): String = value
}