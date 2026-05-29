package com.unicorn.server.domain.member.port.out

// TokenStore - refresh token 저장소 포트를 정의한다.
// 로그아웃 시 삭제, 로그인 시 저장, 재발급 시 조회한다.
interface TokenStore {
	fun save(memberId: String, refreshToken: String)
	fun findMemberIdByRefreshToken(refreshToken: String): String?
	fun deleteByMemberId(memberId: String)
}
