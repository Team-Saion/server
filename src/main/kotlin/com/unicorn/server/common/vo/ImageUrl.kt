package com.unicorn.server.common.vo

import java.net.URI

@JvmInline
value class ImageUrl(val value: String) {

	init {
		require(value.isNotBlank()) { "ImageUrl cannot be blank" }

		val uri = runCatching { URI(value.trim()) }
			.getOrElse { throw IllegalArgumentException("Invalid image URL: $value") }

		require(uri.scheme in SUPPORTED_SCHEMES) { "ImageUrl scheme must be http or https" }
		require(!uri.host.isNullOrBlank()) { "ImageUrl host cannot be blank" }
		require(hasSupportedExtension(uri.path)) { "ImageUrl must have a supported image extension" }
	}

	val extension: String
		get() = value.substringBefore('?')
			.substringBefore('#')
			.substringAfterLast('.', missingDelimiterValue = "")
			.lowercase()

	fun isHttps(): Boolean = value.startsWith("https://", ignoreCase = true)

	override fun toString(): String = value

	companion object {
		private val SUPPORTED_SCHEMES = setOf("http", "https")
		private val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")

		private fun hasSupportedExtension(path: String?): Boolean {
			if (path.isNullOrBlank()) {
				return false
			}

			val extension = path.substringAfterLast('.', missingDelimiterValue = "").lowercase()
			return extension in SUPPORTED_EXTENSIONS
		}
	}
}
