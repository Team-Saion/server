package com.unicorn.server.domain.member.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.port.`in`.GetMemberInPort
import com.unicorn.server.domain.member.port.`in`.LogoutInPort
import com.unicorn.server.domain.member.port.`in`.SocialLoginInPort
import com.unicorn.server.domain.member.port.`in`.UpdateProfileInPort
import com.unicorn.server.domain.member.port.`in`.WithdrawMemberInPort
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// MemberService - 멤버 관련 유스케이스를 조율하는 애플리케이션 서비스다.
@Service
@Transactional
class MemberService(
	private val memberOutPort: MemberOutPort,
	private val socialAccountOutPort: SocialAccountOutPort,
	private val tokenIssuer: TokenIssuer,
	private val tokenStore: TokenStore,
	private val eventPublisher: EventPublisher,
) : SocialLoginInPort, GetMemberInPort, UpdateProfileInPort, LogoutInPort, WithdrawMemberInPort {

	// 소셜 로그인 유스케이스를 처리한다.
	override fun login(command: SocialLoginCommand): TokenPair {
		// TODO: Step 4 - 카카오 ID Token 검증 후 Member/SocialAccount 조회 또는 신규 생성,
		//               JWT 발급 로직 구현. 초기 nickname = command.name 으로 세팅.
		TODO("소셜 로그인은 Step 4에서 구현")
	}

	// 멤버 식별자로 저장된 멤버를 조회한다.
	override fun getById(memberId: String): Member = findMemberOrThrow(memberId)

	// 멤버 프로필을 조회, 변경, 저장한다.
	override fun updateProfile(memberId: String, command: UpdateProfileCommand): Member {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 도메인 상태 변경
		member.updateProfile(command.nickname)

		// 변경 데이터 저장
		return memberOutPort.save(member)
	}

	// 멤버 로그아웃 요청을 처리한다.
	override fun logout(memberId: String) {
		// 데이터 조회
		findMemberOrThrow(memberId)

		// refresh token 삭제
		tokenStore.deleteByMemberId(memberId)
	}

	// 멤버를 soft delete 처리한다.
	override fun withdraw(memberId: String) {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 도메인 상태 변경
		member.withdraw()

		// 변경 데이터 저장
		val savedMember = memberOutPort.save(member)

		// 탈퇴 이벤트 발행
		eventPublisher.publish(MemberWithdrawnEvent(savedMember.id.toString()))
	}

	// 멤버 식별자로 도메인을 조회하고 없으면 도메인 예외를 던진다.
	private fun findMemberOrThrow(memberId: String): Member =
		memberOutPort.findById(MemberId.of(memberId)) ?: throw MemberNotFoundException(memberId)
}
