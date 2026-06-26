package com.unicorn.server.infrastructure.adapter.`in`.web.term

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.term.dto.AgreeTermsRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.term.dto.TermResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

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

			**실패 응답**
			- 이 API는 요청 파라미터가 없어 입력 검증 오류(400)는 발생하지 않습니다.
			- 저장된 약관 데이터가 손상되었거나(`term_code`가 알 수 없는 값 등) DB 조회 자체가
			  실패하면 500 응답을 반환합니다. 아래 에러 코드 예시를 참고하세요.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = TermErrorCode::class, code = "INVALID_TERM_DATA"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INTERNAL_SERVER_ERROR"),
	)
	@ApiSuccessCodeExample(TermResponse::class)
	fun getActiveTerms(): ApiResponse<List<TermResponse>>

	@Operation(
		summary = "약관 동의",
		description = """
			인증된 멤버가 가입/온보딩에 필요한 약관 동의 내역을 저장합니다.

			- `termIds`에는 현재 활성 필수 약관 ID가 모두 포함되어야 합니다.
			- 현재 활성 약관 목록에 없는 ID가 포함되면 400 응답을 반환합니다.
			- PENDING, MEMBER, ADMIN 권한의 access token으로 호출할 수 있습니다.
			- 필수 약관이 누락되면 400 응답을 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = TermErrorCode::class, code = "REQUIRED_TERMS_NOT_AGREED"),
		ApiErrorCodeExample(codeType = TermErrorCode::class, code = "INVALID_TERM_ID"),
	)
	fun agreeTerms(
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: AgreeTermsRequest,
	): ApiResponse<Unit>
}
