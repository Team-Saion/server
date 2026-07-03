package com.unicorn.server.domain.member

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.member.vo.SocialAccountId
import java.time.LocalDateTime

// SocialAccount 도메인 - 외부 소셜 계정과 서비스 멤버의 연결 정보를 담당한다.
class SocialAccount internal constructor(
	val id: SocialAccountId,
	val memberId: MemberId,
	val provider: SocialProvider,
	val providerId: String,
	// 소셜 플랫폼에서 받은 이메일(참고용). 다른 플랫폼 확장을 고려해 nullable로 선언한다.
	val email: String?,
	val kakaoNickname: String?,
	val kakaoProfileImageUrl: String?,
	val createdAt: LocalDateTime,
) {
	companion object {
		// 신규 소셜 계정 연결 정보를 생성한다.
		fun create(
			id: SocialAccountId,
			memberId: MemberId,
			provider: SocialProvider,
			providerId: String,
			email: String?,
			kakaoNickname: String?,
			kakaoProfileImageUrl: String?,
		): SocialAccount = SocialAccount(
			id = id,
			memberId = memberId,
			provider = provider,
			providerId = providerId,
			email = email,
			kakaoNickname = kakaoNickname,
			kakaoProfileImageUrl = kakaoProfileImageUrl,
			createdAt = LocalDateTime.now(),
		)
	}
}
