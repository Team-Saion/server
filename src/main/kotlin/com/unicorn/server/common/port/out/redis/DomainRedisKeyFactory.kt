package com.unicorn.server.common.port.out.redis

/**
 * 도메인(기능)뱔 키 생성 전략을 적용하여 Redis Key를 생성합니다
 * 해당 클래스를 상속하는 클래스를 생성하고 key()를 호출하여 Key를 생성할 수 있습니다.
 */
abstract class DomainRedisKeyFactory(
	private val domain: String,
) {
	init {
		require(domain.isNotBlank()) { "Redis key domain cannot be blank" }
	}

	protected fun key(vararg parts: Any): RedisKey {
		require(parts.isNotEmpty()) { "Redis key parts cannot be empty" }

		val suffix = parts.joinToString(KEY_SEPARATOR) { part ->
			part.toString().also {
				require(it.isNotBlank()) { "Redis key part cannot be blank" }
			}
		}

		return RedisKey("$domain$KEY_SEPARATOR$suffix")
	}

	companion object {
		private const val KEY_SEPARATOR = ":"
	}
}
