package com.unicorn.server.domain.member.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.DuplicateEmailException
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.port.`in`.GetMemberInPort
import com.unicorn.server.domain.member.port.`in`.KakaoLoginInPort
import com.unicorn.server.domain.member.port.`in`.LogoutInPort
import com.unicorn.server.domain.member.port.`in`.SocialLoginInPort
import com.unicorn.server.domain.member.port.`in`.UpdateProfileInPort
import com.unicorn.server.domain.member.port.`in`.WithdrawMemberInPort
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.out.KakaoAuthPort
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// MemberService - 멤버 관련 유스케이스를 조율하는 애플리케이션 서비스다.
@Service
@Transactional(readOnly = true)
class MemberService(
	private val memberOutPort: MemberOutPort,
	private val socialAccountOutPort: SocialAccountOutPort,
	private val kakaoAuthPort: KakaoAuthPort,
	private val tokenIssuer: TokenIssuer,
	private val tokenStore: TokenStore,
	private val eventPublisher: EventPublisher,
) : KakaoLoginInPort, SocialLoginInPort, GetMemberInPort, UpdateProfileInPort, LogoutInPort, WithdrawMemberInPort {

	// 카카오 ID Token을 검증하고 소셜 로그인을 처리한다.
	@Transactional
	override fun kakaoLogin(idToken: String): TokenPair {
		val userInfo = kakaoAuthPort.verify(idToken)
		return login(
			SocialLoginCommand(
				provider = SocialProvider.KAKAO,
				providerId = userInfo.providerId,
				email = userInfo.email,
				name = userInfo.name,
			),
		)
	}

	// 검증된 소셜 사용자 정보로 신규 가입 또는 기존 로그인을 처리하고 토큰을 발급한다.
	@Transactional
	override fun login(command: SocialLoginCommand): TokenPair {
		val member = findOrCreateMember(command)

		if (member.isDeleted()) {
			throw MemberNotFoundException(member.id.toString())
		}

		val tokenPair = tokenIssuer.issue(member.id.toString(), member.role)
		tokenStore.save(member.id.toString(), tokenPair.refreshToken)
		return tokenPair
	}

	// 멤버 식별자로 저장된 멤버를 조회한다.
	override fun getById(memberId: String): Member = findMemberOrThrow(memberId)

	// 멤버 프로필을 조회, 변경, 저장한다.
	@Transactional
	override fun updateProfile(memberId: String, command: UpdateProfileCommand): Member {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 도메인 상태 변경
		member.updateProfile(command.nickname)

		// 변경 데이터 저장
		return memberOutPort.save(member)
	}

	// 멤버 로그아웃 요청을 처리한다.
	@Transactional
	override fun logout(memberId: String) {
		// 데이터 조회
		findMemberOrThrow(memberId)

		// refresh token 삭제
		tokenStore.deleteByMemberId(memberId)
	}

	// 멤버를 soft delete 처리한다.
	@Transactional
	override fun withdraw(memberId: String) {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 도메인 상태 변경
		member.withdraw()

		// 변경 데이터 저장
		val savedMember = memberOutPort.save(member)

		// refresh token 삭제
		tokenStore.deleteByMemberId(memberId)

		// 탈퇴 이벤트 발행
		eventPublisher.publish(MemberWithdrawnEvent(savedMember.id.toString()))
	}

	// 소셜 계정으로 기존 멤버를 찾거나 신규 멤버와 소셜 계정을 생성한다.
	private fun findOrCreateMember(command: SocialLoginCommand): Member {
		// 소셜 계정 조회
		val existingSocialAccount = socialAccountOutPort.findByProviderAndProviderId(
			command.provider,
			command.providerId,
		)

		if (existingSocialAccount != null) {
			return memberOutPort.findById(existingSocialAccount.memberId)
				?: throw MemberNotFoundException(existingSocialAccount.memberId.toString())
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
	private fun findMemberOrThrow(memberId: String): Member =
		memberOutPort.findById(MemberId.of(memberId)) ?: throw MemberNotFoundException(memberId)

	companion object {
		private const val MIN_NICKNAME_LENGTH = 2
		private const val MAX_NICKNAME_LENGTH = 30
	}
}
