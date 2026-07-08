package com.unicorn.server.common.port.out.storage

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ObjectType unit test")
class ObjectTypeTest {
	@Test
	@DisplayName("Negative content length is rejected")
	fun validate_withNegativeContentLength_throwsException() {
		assertThatThrownBy { ObjectType.PROFILE_IMAGE.validate("image/jpeg", -1) }
			.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Content length must be non-negative")
	}

	@Test
	@DisplayName("Unsafe extension characters are removed")
	fun generateObjectKey_withUnsafeExtension_keepsOnlyLettersAndDigits() {
		val objectKey = ObjectType.PROFILE_IMAGE.generateObjectKey("profile.jp@g")

		assertThat(objectKey).matches("images/profile/[0-9a-f-]+\\.jpg")
	}
}
