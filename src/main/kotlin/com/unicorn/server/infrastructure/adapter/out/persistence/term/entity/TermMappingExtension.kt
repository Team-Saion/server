package com.unicorn.server.infrastructure.adapter.out.persistence.term.entity

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.vo.MemberTermId
import com.unicorn.server.domain.term.vo.TermId


// ========== TermEntity ↔ Term ==========

/**
 * TermEntity를 Term 도메인 객체로 복원한다.
 *
 * id/effectiveAt/createdAt/updatedAt은 DB NOT NULL 컬럼이라 정상적으로 저장된 row라면
 * 항상 채워져 있어야 한다. null이면 영속화 전 엔티티를 잘못 변환하려는 것이거나
 * 데이터가 손상된 것이므로 BusinessException(INVALID_TERM_DATA)으로 명확하게 알린다.
 * required_yn도 "Y"/"N" 외의 값이면 같은 예외로 처리한다 - 필수 동의 약관이
 * 데이터 오류로 조용히 선택 동의로 취급되는 사고를 막기 위함이다.
 */
fun TermEntity.toDomain(): Term {
	val persistedId = this.id
		?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "term_id가 없는(영속화 전) 엔티티입니다")
	val persistedEffectiveAt = this.effectiveAt
		?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "effective_at이 없습니다")
	val persistedCreatedAt = this.createdAt
		?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "created_at이 없습니다")
	val persistedUpdatedAt = this.updatedAt
		?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "updated_at이 없습니다")
	val required = when (this.requiredYn) {
		"Y" -> true
		"N" -> false
		else -> throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "required_yn 값이 올바르지 않습니다: ${this.requiredYn}")
	}

	return Term(
		id = TermId.of(persistedId),
		termCode = this.termCode,
		title = this.title,
		contentUrl = this.contentUrl,
		version = this.version,
		required = required,
		effectiveAt = persistedEffectiveAt,
		createdAt = persistedCreatedAt,
		updatedAt = persistedUpdatedAt,
	)
}

// ========== MemberTermEntity ↔ MemberTerm ==========

/**
 * MemberTerm 도메인 객체를 MemberTermEntity로 변환한다.
 */
fun MemberTerm.toEntity(): MemberTermEntity = MemberTermEntity().apply {
	memberId = this@toEntity.memberId.toString()
	termId = this@toEntity.termId.value
	agreedAt = this@toEntity.agreedAt
}

/**
 * MemberTermEntity를 MemberTerm 도메인 객체로 복원한다.
 */
fun MemberTermEntity.toDomain(): MemberTerm = MemberTerm(
	id = MemberTermId.of(requireNotNull(this.id) { "id must not be null" }),
	memberId = MemberId.of(this.memberId),
	termId = TermId.of(this.termId),
	agreedAt = requireNotNull(this.agreedAt) { "agreedAt must not be null" },
)
