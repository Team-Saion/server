package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.home.port.dto.HomeMemberDto
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.AvatarColorResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "써클 홈 구성원 응답")
data class CircleMemberResponse(
    @field:Schema(description = "멤버 식별자", example = "00000000-0000-0000-0000-000000000001")
    val memberId: String,

    @field:Schema(description = "써클에서 표시할 구성원 닉네임", example = "길동이")
    val nickname: String,

    @field:Schema(description = "멤버 아바타 기본 색상")
    val avatarColor: AvatarColorResponse,

    @field:Schema(
        description = "프로필 이미지 객체 URL. 설정하지 않았으면 null이다.",
        example = "https://dev.saion.app/images/profile/00000000-0000-0000-0000-000000000001.png",
        nullable = true,
    )
    val profileImageUrl: String?,

    @field:Schema(description = "요청자 본인 여부", example = "true")
    val isMe: Boolean,

    @field:Schema(description = "써클 내 역할", example = "MEMBER")
    val role: String,
) {
    companion object {
        fun from(view: HomeMemberDto, serverUrl: String): CircleMemberResponse = CircleMemberResponse(
            memberId = view.memberId,
            nickname = view.nickname,
            avatarColor = AvatarColorResponse.from(view.avatarColor),
            profileImageUrl = view.profileImageKey?.let { "$serverUrl/$it" },
            isMe = view.isMe,
            role = view.role
        )
    }
}
