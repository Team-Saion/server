package com.unicorn.server.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
class DiscordConfig {
    @Bean
    fun discordRestTemplate(): RestTemplate = RestTemplate()
}
