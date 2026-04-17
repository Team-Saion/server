package com.unicorn.server.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
	@param:Value("\${spring.data.redis.host:localhost}")
	private val host: String,

	@param:Value("\${spring.data.redis.port:6379}")
	private val port: Int,

	@param:Value("\${spring.data.redis.password:}")
	private val password: String,
) {
	@Bean
	fun redisConnectionFactory(): RedisConnectionFactory {
		val redisConfiguration = RedisStandaloneConfiguration(host, port)
		if (password.isNotBlank()) {
			redisConfiguration.setPassword(password)
		}
		return LettuceConnectionFactory(redisConfiguration)
	}

	@Bean
	fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
		RedisTemplate<String, String>().apply {
			connectionFactory = redisConnectionFactory
			keySerializer = StringRedisSerializer()
			valueSerializer = StringRedisSerializer()
			hashKeySerializer = StringRedisSerializer()
			hashValueSerializer = StringRedisSerializer()
		}
}
