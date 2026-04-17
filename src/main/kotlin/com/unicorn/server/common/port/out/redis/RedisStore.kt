package com.unicorn.server.common.port.out.redis

import java.time.Duration

interface RedisStore {
	fun set(key: RedisKey, value: String)

	fun set(key: RedisKey, value: String, ttl: Duration)

	fun get(key: RedisKey): String?

	fun delete(key: RedisKey): Boolean

	fun exists(key: RedisKey): Boolean

	fun expire(key: RedisKey, ttl: Duration): Boolean
}
