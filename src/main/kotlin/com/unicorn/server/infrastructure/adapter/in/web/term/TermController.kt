package com.unicorn.server.infrastructure.adapter.`in`.web.term

import com.unicorn.server.domain.term.port.`in`.GetActiveTermsInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.term.dto.TermResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
공개 약관 목록 조회 엔드포인트를 처리하는 컨트롤러다.

- 인증이 필요 없다. 회원가입 전(가입 화면 진입 시)에 호출되기 때문이다.
- SecurityConfig.PERMIT_ALL_ENDPOINTS에 "/v1/terms" 하위 경로를 추가해야 401 없이 접근 가능하다.
*/
@RestController
@RequestMapping("/v1/terms")
class TermController(
	private val getActiveTermsInPort: GetActiveTermsInPort,
) : TermApiDoc {

	// GET /v1/terms - term_code별 현재 발효 중인 최신 버전 목록을 조회한다.
	@GetMapping
	override fun getActiveTerms(): ApiResponse<List<TermResponse>> {
		val terms = getActiveTermsInPort.getActiveTerms()
		return ApiResponse.success(terms.map { TermResponse.from(it) })
	}
}
