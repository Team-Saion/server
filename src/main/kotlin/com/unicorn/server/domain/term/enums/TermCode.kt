package com.unicorn.server.domain.term.enums

/**
약관 유형(카테고리)을 표현하는 코드 값이다.

- 버전과 독립적으로 유지된다. 즉 같은 TermCode 안에서 여러 version의 row가 존재할 수 있다.
- 새로운 약관 유형이 추가되면 이 enum에 값을 추가하고, TB_TERM에 해당 term_code로 row를 적재한다.
- 이미 적재된 TB_TERM.term_code 문자열과 enum 이름이 어긋나지 않도록 값 변경/삭제 시 주의해야 한다.
*/
enum class TermCode {
	AGE_OVER_14,
	SERVICE_USE,
	PRIVACY_COLLECTION,
	MARKETING,
}
