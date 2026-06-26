package com.unicorn.server.infrastructure.adapter.out.persistence.term.entity

import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.vo.MemberTermId
import com.unicorn.server.domain.term.vo.TermId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
	name = "member_agreement",
	indexes = [
		Index(name = "idx_member_agreement_member_id", columnList = "member_id"),
		Index(name = "idx_member_agreement_term_id", columnList = "term_id"),
	],
)
@EntityListeners(AuditingEntityListener::class)
class MemberTermEntity protected constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_agreement_id", nullable = false)
	var id: Long? = null
		protected set

	@Column(name = "member_id", nullable = false, length = 36)
	var memberId: String = ""
		protected set

	@Column(name = "term_id", nullable = false)
	var termId: Long = 0L
		protected set

	@CreatedDate
	@Column(name = "agreed_at", nullable = false, updatable = false)
	var agreedAt: LocalDateTime? = null
		protected set

	constructor(memberTerm: MemberTerm) : this() {
		memberId = memberTerm.memberId.toString()
		termId = memberTerm.termId.value
	}

	fun toDomain(): MemberTerm = MemberTerm.reconstitute(
		id = MemberTermId.of(requireNotNull(id) { "id must not be null" }),
		memberId = MemberId.of(memberId),
		termId = TermId.of(termId),
		agreedAt = requireNotNull(agreedAt) { "agreedAt must not be null" },
	)
}
