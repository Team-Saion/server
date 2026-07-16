package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.home.port.dto.HomeMemberDto
import com.unicorn.server.domain.member.enums.AvatarColor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CircleMemberResponse 단위 테스트")
class CircleMemberResponseTest {
	@Test
	@DisplayName("홈 멤버 응답 변환 시 프로필 이미지 URL과 아바타 색상 정보를 포함한다")
	fun from_includesProfileImageUrlAndAvatarColor() {
		val view = HomeMemberDto(
			memberId = "member-id",
			nickname = "닉네임",
			avatarColor = AvatarColor.TEAL_200,
			profileImageKey = "images/profile/member-id.png",
			isMe = true,
			role = "MEMBER",
		)

		val response = CircleMemberResponse.from(view, "https://dev.saion.app")

		assertThat(response.profileImageUrl).isEqualTo("https://dev.saion.app/images/profile/member-id.png")
		assertThat(response.avatarColor.code).isEqualTo("Color/teal/200")
		assertThat(response.avatarColor.hex).isEqualTo("#84D6D6")
	}
}
