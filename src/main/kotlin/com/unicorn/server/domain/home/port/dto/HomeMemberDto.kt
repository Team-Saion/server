package com.unicorn.server.domain.home.port.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unicorn.server.domain.member.enums.AvatarColor

data class HomeMemberDto(
    val memberId: String,
    val nickname: String,
    val avatarColor: AvatarColor,
    val profileImageKey: String?,
    @get:JsonProperty("isMe")
    val isMe: Boolean,
    val role: String,
)
