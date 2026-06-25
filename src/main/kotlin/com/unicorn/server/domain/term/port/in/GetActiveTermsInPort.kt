package com.unicorn.server.domain.term.port.`in`

import com.unicorn.server.domain.term.Term

/**
term_code별 현재 발효 중인 최신 버전 목록을 조회하는 유스케이스 진입점이다.

회원가입 화면에서 보여줄 약관 목록을 가져오는 용도로 사용하며, 인증이 필요 없는
공개 유스케이스다.
*/
interface GetActiveTermsInPort {
	fun getActiveTerms(): List<Term>
}
