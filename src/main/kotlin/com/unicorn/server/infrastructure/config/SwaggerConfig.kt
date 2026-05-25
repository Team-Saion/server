package com.unicorn.server.infrastructure.config

import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.customizer.ExampleResponseCustomizer
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.nio.charset.StandardCharsets

@Configuration
class SwaggerConfig(
	@param:Value("\${app.server.url}")
	private val serverUrl: String,
	@param:Value("classpath:/swagger/description.md")
	private val swaggerDescription: Resource,
) {

	@Bean
	fun openAPI(): OpenAPI =
		OpenAPI()
			.addServersItem(Server().url(serverUrl))
			.info(apiInfo())

	@Bean
	fun generalApi(exampleResponseCustomizer: ExampleResponseCustomizer): GroupedOpenApi =
		GroupedOpenApi.builder()
			.group("general")
			.pathsToMatch("/api/**")
			.addOperationCustomizer(exampleResponseCustomizer)
			.build()

	private fun apiInfo(): Info =
		Info()
			.title("Unicorn Project API")
			.description(loadDescription())
			.version("0.0.1")

	private fun loadDescription(): String =
		swaggerDescription.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

}
