package com.unicorn.server.infrastructure.adapter.out.persistence.term.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
	name = "member_agreement",
	uniqueConstraints = [
		UniqueConstraint(
			name = "uk_member_agreement_member_id_term_id",
			columnNames = ["member_id", "term_id"],
		),
	],
	indexes = [
		Index(name = "idx_member_agreement_member_id", columnList = "member_id"),
		Index(name = "idx_member_agreement_term_id", columnList = "term_id"),
	],
)
class MemberTermEntity internal constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_agreement_id", nullable = false)
	var id: Long? = null
		internal set

	@Column(name = "member_id", nullable = false, length = 21)
	var memberId: String = ""
		internal set

	@Column(name = "term_id", nullable = false)
	var termId: Long = 0L
		internal set

	@Column(name = "agreed_at", nullable = false, updatable = false)
	var agreedAt: LocalDateTime? = null
		internal set


}
