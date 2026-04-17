package com.unicorn.server.infrastructure.adapter.out.redis

import com.unicorn.server.common.port.out.redis.RedisKey
import com.unicorn.server.common.port.out.redis.RedisStore
import com.unicorn.server.infrastructure.adapter.out.redis.exception.RedisOperationFailedException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisStoreAdapter(
	private val redisTemplate: RedisTemplate<String, String>,
) : RedisStore {
	override fun set(key: RedisKey, value: String) {
		runRedis("set", key) {
			redisTemplate.opsForValue().set(key.value, value)
		}
	}

	override fun set(key: RedisKey, value: String, ttl: Duration) {
		runRedis("setWithTtl", key) {
			redisTemplate.opsForValue().set(key.value, value, ttl)
		}
	}

	override fun get(key: RedisKey): String? =
		runRedis("get", key) {
			redisTemplate.opsForValue().get(key.value)
		}

	override fun delete(key: RedisKey): Boolean =
		runRedis("delete", key) {
			redisTemplate.delete(key.value) == true
		}

	override fun exists(key: RedisKey): Boolean =
		runRedis("exists", key) {
			redisTemplate.hasKey(key.value) == true
		}

	override fun expire(key: RedisKey, ttl: Duration): Boolean =
		runRedis("expire", key) {
			redisTemplate.expire(key.value, ttl) == true
		}

	private fun <T> runRedis(operation: String, key: RedisKey, block: () -> T): T =
		runCatching(block).getOrElse { cause ->
			throw RedisOperationFailedException(operation, key.value, cause)
		}
}
