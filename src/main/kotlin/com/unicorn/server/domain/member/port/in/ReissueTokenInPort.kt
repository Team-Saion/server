package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.TokenPair

// ReissueTokenInPort - refresh token으로 새 인증 토큰 쌍을 발급하는 유스케이스를 정의한다.
interface ReissueTokenInPort {
	fun reissue(refreshToken: String): TokenPair
}
