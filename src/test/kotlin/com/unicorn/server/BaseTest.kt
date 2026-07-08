package com.unicorn.server

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

open class BaseTest {
    protected val baseMember = BaseMember()
    protected val testJwtSecret: String = "aqr3l4WUSfDEauYcRmAJPH1TXW9ym3Zs5EPMhzekPI3"
    protected val accessTokenExpirationSeconds: Long = 3600L
    protected val refreshTokenExpirationSeconds: Long = 2592000L

    protected class BaseMember {
        val id: String = "11111111-1111-1111-1111-111111111111"
        val email: String = "local.member@unicorn.test"
        val name: String = "Local Member"
        val nickname: String = "local_member"
        val providerId: String = "kakao-local-member-001"

        fun toDomain(): Member = Member(
            id = MemberId.of(id),
            email = Email(email),
            name = name,
            nickname = nickname,
            role = Role.MEMBER,
            profileImageKey = null,
            status = MemberStatus.ACTIVE,
            deletedAt = null,
            createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            avatarColor = AvatarColor.random(),
        )
    }
}
