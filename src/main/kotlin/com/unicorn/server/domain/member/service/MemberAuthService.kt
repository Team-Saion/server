package com.unicorn.server.domain.member.service

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.exception.DuplicateEmailException
import com.unicorn.server.domain.member.exception.InvalidRefreshTokenException
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.`in`.LogoutInPort
import com.unicorn.server.domain.member.port.`in`.ReissueTokenInPort
import com.unicorn.server.domain.member.port.`in`.SocialLoginInPort
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// MemberAuthService - 공통 소셜 로그인과 로그아웃 유스케이스를 처리한다.
@Service
@Transactional(readOnly = false)
class MemberAuthService(
	private val memberOutPort: MemberOutPort,
	private val socialAccountOutPort: SocialAccountOutPort,
	private val tokenIssuer: TokenIssuer,
	private val tokenStore: TokenStore,
) : SocialLoginInPort, LogoutInPort, ReissueTokenInPort {

	// 검증된 소셜 사용자 정보로 신규 가입 또는 기존 로그인을 처리하고 토큰을 발급한다.
	override fun login(command: SocialLoginCommand): TokenPair {
		val member = findOrCreateMember(command)

		val tokenPair = tokenIssuer.issue(member.id.toString(), member.role)
		tokenStore.save(member.id.toString(), tokenPair.refreshToken)
		return tokenPair
	}

	// 멤버 로그아웃 요청을 처리한다.
	override fun logout(memberId: String) {
		// 데이터 조회
		findMemberOrThrow(memberId)

		// refresh token 삭제
		tokenStore.deleteByMemberId(memberId)
	}

	// 유효하고 현재 활성 상태인 refresh token을 회전해 새 토큰 쌍을 발급한다.
	override fun reissue(refreshToken: String): TokenPair {
		val memberId = tokenIssuer.parseRefreshToken(refreshToken)
			?: throw InvalidRefreshTokenException()

		if (tokenStore.findMemberIdByRefreshToken(refreshToken) != memberId) {
			throw InvalidRefreshTokenException()
		}

		val member = findMemberOrThrow(memberId)
		val tokenPair = tokenIssuer.issue(member.id.toString(), member.role)
		tokenStore.save(member.id.toString(), tokenPair.refreshToken)
		return tokenPair
	}

	// 소셜 계정으로 기존 멤버를 찾거나 신규 멤버와 소셜 계정을 생성한다.
	private fun findOrCreateMember(command: SocialLoginCommand): Member {
		// 소셜 계정 조회
		val existingSocialAccount = socialAccountOutPort.findByProviderAndProviderId(
			command.provider,
			command.providerId,
		)

		if (existingSocialAccount != null) {
			return findMemberOrThrow(existingSocialAccount.memberId.toString())
		}

		// 이메일 중복 검증
		if (memberOutPort.existsByEmail(Email(command.email))) {
			throw DuplicateEmailException(command.email)
		}

		// 멤버 및 소셜 계정 생성
		val newMember = memberOutPort.save(
			Member.create(
				email = Email(command.email),
				name = command.name,
				nickname = toSafeNickname(command.name),
			),
		)

		socialAccountOutPort.save(
			SocialAccount.create(
				memberId = newMember.id,
				provider = command.provider,
				providerId = command.providerId,
				email = command.email,
			),
		)

		return newMember
	}

	// 외부 플랫폼 이름을 멤버 닉네임 제약에 맞게 보정한다.
	private fun toSafeNickname(name: String): String {
		val trimmed = name.trim().take(MAX_NICKNAME_LENGTH)
		return if (trimmed.length < MIN_NICKNAME_LENGTH) {
			trimmed.padEnd(MIN_NICKNAME_LENGTH, '_')
		} else {
			trimmed
		}
	}

	// 멤버 식별자로 도메인을 조회하고 없으면 도메인 예외를 던진다.
	private fun findMemberOrThrow(memberId: String): Member {
		val member = memberOutPort.findById(MemberId.of(memberId)) ?: throw MemberNotFoundException(memberId)
		if (member.isDeleted()) {
			throw WithdrawnMemberException(memberId)
		}
		return member
	}

	companion object {
		private const val MIN_NICKNAME_LENGTH = 2
		private const val MAX_NICKNAME_LENGTH = 30
	}
}
