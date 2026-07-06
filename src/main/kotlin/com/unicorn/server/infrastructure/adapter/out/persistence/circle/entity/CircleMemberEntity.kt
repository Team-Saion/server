package com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "circle_member",
    indexes = [
        Index(name = "idx_circle_member_member_id", columnList = "member_id"),
        Index(name = "idx_circle_member_circle_id", columnList = "circle_id"),
    ],
)
class CircleMemberEntity internal constructor() : AuditableJpaEntity() {
    @Id
    @Column(name = "id", nullable = false, length = 21)
    var id: String = ""
        internal set

    @Column(name = "circle_id", nullable = false, length = 21)
    var circleId: String = ""
        internal set

    @Column(name = "member_id", nullable = false, length = 36)
    var memberId: String = ""
        internal set

    @Column(name = "nickname", nullable = false, length = 30)
    var nickname: String = ""
        internal set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    lateinit var role: CircleRole
        internal set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    lateinit var status: CircleMemberStatus
        internal set

    @Column(name = "joined_at", nullable = false)
    lateinit var joinedAt: LocalDateTime
        internal set

    @Column(name = "left_at")
    var leftAt: LocalDateTime? = null
        internal set

    @Column(name = "del_yn", nullable = false, length = 1)
    var delYn: String = "N"
        internal set


}
