package com.unicorn.server.domain.term.vo

/**
TermId 값 객체.

term_id는 애플리케이션이 생성하지 않고 DB가 발급하는 BIGINT auto-increment 값이다.
그래서 Member/SocialAccount의 MemberId(UUID)와 달리 generate() 팩토리가 없고,
저장 전에는 식별자가 존재하지 않는다. 약관 등록 API가 아직 없어 Term.create() 같은
사전 생성 팩토리도 함께 없는 상태다.
*/
@JvmInline
value class TermId(val value: Long) {
	override fun toString(): String = value.toString()

	companion object {
		fun of(value: Long): TermId = TermId(value)
	}
}
