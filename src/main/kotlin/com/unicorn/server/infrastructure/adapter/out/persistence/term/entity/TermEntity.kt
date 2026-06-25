package com.unicorn.server.infrastructure.adapter.out.persistence.term.entity

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.vo.TermId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
Term 도메인을 JPA 테이블 row로 저장/복원하는 영속성 엔티티다.

- TB_TERM 설계상 created_by/updated_by 컬럼이 없다(TB_MEMBER_AGREEMENT와 다름).
  그래서 다른 엔티티들과 달리 AuditableJpaEntity를 상속하지 않고
  created_at/updated_at만 직접 선언해서 auditing 처리한다.
- term_id는 DB가 발급하는 IDENTITY 컬럼이라 protected set의 var Long?로 두고,
  저장 전에는 null이다. toDomain()은 이 시점에 호출되면 BusinessException을 던진다.
- (term_code, version) 조합에 유니크 제약을 걸어 같은 버전이 중복 저장되는 것을
  DB 레벨에서도 막는다.
- raw 생성자는 약관 등록 API가 아직 없는 상태에서 테스트/시딩 데이터를 넣기 위한
  임시 수단이다. 등록 API가 생기면 Term.create() 기반 생성자로 대체될 수 있다.
*/
@Entity
@Table(
	name = "term",
	uniqueConstraints = [UniqueConstraint(name = "uq_term_code_version", columnNames = ["term_code", "version"])],
	indexes = [Index(name = "idx_term_code_effective", columnList = "term_code, effective_at")],
)
@EntityListeners(AuditingEntityListener::class)
class TermEntity protected constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "term_id", nullable = false)
	var id: Long? = null
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "term_code", nullable = false, length = 50)
	lateinit var termCode: TermCode
		protected set

	@Column(name = "title", nullable = false, length = 100)
	var title: String = ""
		protected set

	@Column(name = "content_url", length = 500)
	var contentUrl: String? = null
		protected set

	@Column(name = "version", nullable = false)
	var version: Int = 0
		protected set

	@Column(name = "required_yn", nullable = false, length = 1)
	var requiredYn: String = "N"
		protected set

	@Column(name = "effective_at", nullable = false)
	var effectiveAt: LocalDateTime? = null
		protected set

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: LocalDateTime? = null
		protected set

	@LastModifiedDate
	@Column(name = "updated_at")
	var updatedAt: LocalDateTime? = null
		protected set

	// 테스트/시딩 용도 raw 생성자 (Term.create() 팩토리가 없어 컬럼 값을 직접 받는다)
	constructor(
		termCode: TermCode,
		title: String,
		contentUrl: String?,
		version: Int,
		requiredYn: String,
		effectiveAt: LocalDateTime,
	) : this() {
		this.termCode = termCode
		this.title = title
		this.contentUrl = contentUrl
		this.version = version
		this.requiredYn = requiredYn
		this.effectiveAt = effectiveAt
	}

	/**
	영속성 객체를 순수 도메인 객체로 복원한다.

	id/effectiveAt/createdAt/updatedAt은 DB NOT NULL 컬럼이라 정상적으로 저장된 row라면
	항상 채워져 있어야 한다. null이면 영속화 전 엔티티를 잘못 변환하려는 것이거나
	데이터가 손상된 것이므로 BusinessException(INVALID_TERM_DATA)으로 명확하게 알린다.
	required_yn도 "Y"/"N" 외의 값이면 같은 예외로 처리한다 - 필수 동의 약관이
	데이터 오류로 조용히 선택 동의로 취급되는 사고를 막기 위함이다.
	*/
	fun toDomain(): Term {
		val persistedId = id
			?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "term_id가 없는(영속화 전) 엔티티입니다")
		val persistedEffectiveAt = effectiveAt
			?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "effective_at이 없습니다")
		val persistedCreatedAt = createdAt
			?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "created_at이 없습니다")
		val persistedUpdatedAt = updatedAt
			?: throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "updated_at이 없습니다")
		val required = when (requiredYn) {
			"Y" -> true
			"N" -> false
			else -> throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "required_yn 값이 올바르지 않습니다: $requiredYn")
		}

		return Term.reconstitute(
			id = TermId.of(persistedId),
			termCode = termCode,
			title = title,
			contentUrl = contentUrl,
			version = version,
			required = required,
			effectiveAt = persistedEffectiveAt,
			createdAt = persistedCreatedAt,
			updatedAt = persistedUpdatedAt,
		)
	}
}
