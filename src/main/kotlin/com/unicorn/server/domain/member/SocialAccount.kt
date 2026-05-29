package com.unicorn.server.domain.member

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.member.vo.SocialAccountId
import java.time.LocalDateTime

// SocialAccount 도메인 - 외부 소셜 계정과 서비스 멤버의 연결 정보를 담당한다.
class SocialAccount private constructor(
	val id: SocialAccountId,
	val memberId: MemberId,
	val provider: SocialProvider,
	val providerId: String,
	// 소셜 플랫폼에서 받은 이메일(참고용). 다른 플랫폼 확장을 고려해 nullable로 선언한다.
	val email: String?,
	val createdAt: LocalDateTime,
) {
	companion object {
		// 신규 소셜 계정 연결 정보를 생성한다.
		fun create(
			memberId: MemberId,
			provider: SocialProvider,
			providerId: String,
			email: String?,
		): SocialAccount = SocialAccount(
			id = SocialAccountId.generate(),
			memberId = memberId,
			provider = provider,
			providerId = providerId,
			email = email,
			createdAt = LocalDateTime.now(),
		)

		// 저장소의 원시 상태를 도메인 소셜 계정으로 복원한다.
		fun reconstitute(
			id: SocialAccountId,
			memberId: MemberId,
			provider: SocialProvider,
			providerId: String,
			email: String?,
			createdAt: LocalDateTime,
		): SocialAccount = SocialAccount(id, memberId, provider, providerId, email, createdAt)
	}
}
