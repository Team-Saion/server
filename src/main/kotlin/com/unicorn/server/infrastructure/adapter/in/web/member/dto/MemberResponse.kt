package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

// MemberResponse - 멤버 조회/변경 결과를 외부로 전달하는 응답 DTO다.
@Schema(description = "멤버 프로필 응답")
data class MemberResponse(
	@field:Schema(
		description = "멤버 식별자",
		example = "00000000-0000-0000-0000-000000000001",
	)
	val id: String,

	@field:Schema(
		description = "서비스 내 고유 이메일",
		example = "user@example.com",
		nullable = true,
	)
	val email: String?,

	@field:Schema(
		description = "소셜 플랫폼에서 제공한 이름",
		example = "홍길동",
		nullable = true,
	)
	val name: String?,

	@field:Schema(
		description = "서비스 내 노출 닉네임",
		example = "길동이",
	)
	val nickname: String,

	@field:Schema(
		description = "멤버 역할",
		example = "MEMBER",
		allowableValues = ["PENDING", "MEMBER", "ADMIN"],
	)
	val role: Role,

	@field:Schema(description = "멤버 아바타 기본 색상")
	val avatarColor: AvatarColorResponse,

	@field:Schema(
		description = "프로필 이미지 객체 키. 설정하지 않았으면 null이다.",
		example = "member/profile/00000000-0000-0000-0000-000000000001.png",
		nullable = true,
	)
	val profileImageKey: String?,

	@field:Schema(
		description = "프로필 이미지 객체 URL. 설정하지 않았으면 null이다.",
		example = "https://dev.saion.app/images/profile/00000000-0000-0000-0000-000000000001.png",
		nullable = true,
	)
	val profileImageUrl: String?,

	@field:Schema(
		description = "멤버 상태",
		example = "ACTIVE",
		allowableValues = ["ACTIVE", "DELETED"],
	)
	val status: MemberStatus,

	@field:Schema(
		description = "멤버 생성 시각",
		example = "2024-01-01T00:00:00",
	)
	val createdAt: LocalDateTime,
) {
	companion object {
		// 도메인 멤버를 응답 DTO로 변환한다. profileImageUrl은 objectKey로부터 호출부(어댑터)에서 미리 변환해 전달한다.
		fun from(member: Member, profileImageUrl: String?): MemberResponse = MemberResponse(
			id = member.id.toString(),
			email = member.email?.value,
			name = member.name,
			nickname = member.nickname,
			role = member.role,
			avatarColor = AvatarColorResponse.from(member.avatarColor),
			profileImageKey = member.profileImageKey,
			profileImageUrl = profileImageUrl,
			status = member.status,
			createdAt = member.createdAt,
		)
	}
}
