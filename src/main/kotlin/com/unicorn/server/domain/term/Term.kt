package com.unicorn.server.domain.term

import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.vo.TermId
import java.time.LocalDateTime

/**
약관(Term)의 특정 버전 1건을 표현하는 도메인 객체다.

- 하나의 termCode 안에 여러 version이 존재할 수 있고, 이 클래스는 그중 한 row를 가리킨다.
- "현재 유효한 최신 버전" 판단은 이 클래스가 아니라 TermQueryService가 한다. Term 자체는
  자신이 최신인지 알지 못한다.
- 조회 전용 도메인이라 상태 변경 메서드가 없다. 약관 등록/개정 API가 추가되면 그때
  create() 팩토리와 변경 메서드가 필요해질 수 있다.
- TB_TERM은 append-only로 운영된다(기존 row를 UPDATE하지 않고 새 버전 row를 추가).
  따라서 이 객체도 일단 만들어지면 불변으로 다루는 것이 자연스럽다.
*/
class Term internal constructor(
	val id: TermId,
	val termCode: TermCode,
	val title: String,
	val contentUrl: String?,
	val version: Int,
	val required: Boolean,
	val effectiveAt: LocalDateTime,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)
