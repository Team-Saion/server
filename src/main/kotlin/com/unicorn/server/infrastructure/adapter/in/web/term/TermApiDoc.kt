package com.unicorn.server.infrastructure.adapter.`in`.web.term

import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.term.dto.TermResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

/**
약관 조회 API의 Swagger 문서를 정의하는 인터페이스다.

실제 라우팅/구현은 TermController가 담당하고, 이 인터페이스는 OpenAPI 문서 생성을 위한
애노테이션만 담는다(기존 MemberApiDoc/AuthApiDoc과 동일한 패턴).
*/
@Tag(name = "Term API", description = "약관 조회 API")
interface TermApiDoc {

	@Operation(
		summary = "활성 약관 목록 조회",
		description = """
			현재 발효 중인 약관을 약관 유형(`termCode`)별로 가장 높은 버전 1건씩 조회합니다.

			**선택 규칙**
			- 같은 `termCode`에 여러 버전이 존재할 수 있습니다. 그중 `effectiveAt <= 현재 시각`인
			  버전들만 후보로 보고, 후보 중 `version`이 가장 높은 1건만 응답에 포함합니다.
			- 아직 발효되지 않은(`effectiveAt`이 미래인) 버전은 후보에서 제외되어 응답에 나타나지 않습니다.

			**호출 조건**
			- 인증이 필요 없는 공개 API입니다. 회원가입 화면 진입 시점, 즉 로그인 전에도 호출할 수 있습니다.

			**응답 활용**
			- `required`가 true인 항목은 모두 동의해야 가입이 가능합니다.
			- 등록된 약관이 없거나 아직 발효된 버전이 없으면 빈 배열(`[]`)을 반환합니다. 이는 오류가
			  아니라 정상적인 200 응답입니다.
		""",
	)
	@ApiSuccessCodeExample(TermResponse::class)
	fun getActiveTerms(): ApiResponse<List<TermResponse>>
}
