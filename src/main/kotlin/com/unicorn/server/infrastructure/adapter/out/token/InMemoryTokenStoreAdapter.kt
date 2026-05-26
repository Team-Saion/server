package com.unicorn.server.infrastructure.adapter.out.token

import com.unicorn.server.domain.member.port.out.TokenStore
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

// InMemoryTokenStoreAdapter - refresh token을 메모리에 저장한다.
// 추후 RedisTokenStoreAdapter로 교체 가능하다.
@Component
class InMemoryTokenStoreAdapter : TokenStore {

	// memberId -> refreshToken
	private val memberToToken = ConcurrentHashMap<String, String>()

	// refreshToken -> memberId
	private val tokenToMember = ConcurrentHashMap<String, String>()

	// 멤버의 refresh token을 저장하고 기존 토큰 인덱스를 정리한다.
	override fun save(memberId: String, refreshToken: String) {
		deleteByMemberId(memberId)
		memberToToken[memberId] = refreshToken
		tokenToMember[refreshToken] = memberId
	}

	// refresh token으로 멤버 식별자를 조회한다.
	override fun findMemberIdByRefreshToken(refreshToken: String): String? =
		tokenToMember[refreshToken]

	// 멤버 식별자로 저장된 refresh token을 삭제한다.
	override fun deleteByMemberId(memberId: String) {
		memberToToken.remove(memberId)?.let { tokenToMember.remove(it) }
	}
}
