package com.unicorn.server.domain.member.port.out

// TokenStore - refresh token 저장소 포트를 정의한다.
// 로그아웃 시 삭제, 로그인 시 저장, 재발급 시 조회한다.
interface TokenStore {
	// 멤버의 refresh token을 저장한다.
	// 이미 저장된 refresh token이 있다면 findMemberIdByRefreshToken으로 더 이상
	// 조회되지 않도록 역방향 매핑까지 함께 무효화한 뒤 새 토큰으로 교체해야 한다.
	fun save(memberId: String, refreshToken: String)
	fun findMemberIdByRefreshToken(refreshToken: String): String?
	fun deleteByMemberId(memberId: String)
}
