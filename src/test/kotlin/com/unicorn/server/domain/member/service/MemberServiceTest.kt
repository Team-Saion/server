package com.unicorn.server.domain.member.service

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

	private val memberOutPort = FakeMemberOutPort()
	private val socialAccountOutPort = FakeSocialAccountOutPort()
	private val tokenIssuer = FakeTokenIssuer()
	private val tokenStore = FakeTokenStore()
	private val eventPublisher = RecordingEventPublisher()
	private val memberService = MemberService(
		memberOutPort,
		socialAccountOutPort,
		tokenIssuer,
		tokenStore,
		eventPublisher,
	)

	// TODO: Step 4 - 소셜 로그인 테스트 추가
	//   login_newMember_createsMemberAndSocialAccount()
	//   login_existingMember_returnsTokenPair()
	//   login_duplicateEmail_differentProvider_throwsDuplicateEmailException()

	@Test
	@DisplayName("getById 호출 시 저장된 멤버를 반환한다")
	fun getById_returnsSavedMember() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		val result = memberService.getById(member.id.toString())

		assertThat(result.id).isEqualTo(member.id)
	}

	@Test
	@DisplayName("존재하지 않는 ID로 getById 호출 시 MemberNotFoundException이 발생한다")
	fun getById_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberService.getById(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("updateProfile 호출 시 닉네임이 변경된다")
	fun updateProfile_nicknameIsUpdated() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		val result = memberService.updateProfile(member.id.toString(), UpdateProfileCommand("새닉네임"))

		assertThat(result.nickname).isEqualTo("새닉네임")
	}

	@Test
	@DisplayName("존재하지 않는 ID로 updateProfile 호출 시 MemberNotFoundException이 발생한다")
	fun updateProfile_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberService.updateProfile(unknownId, UpdateProfileCommand("닉네임")) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("withdraw 호출 시 상태가 DELETED로 변경된다")
	fun withdraw_statusBecomesDeleted() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberService.withdraw(member.id.toString())

		assertThat(memberOutPort.findById(member.id)!!.status).isEqualTo(MemberStatus.DELETED)
	}

	@Test
	@DisplayName("withdraw 호출 시 deletedAt이 세팅된다")
	fun withdraw_deletedAtIsSet() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberService.withdraw(member.id.toString())

		assertThat(memberOutPort.findById(member.id)!!.deletedAt).isNotNull()
	}

	@Test
	@DisplayName("withdraw 호출 시 MemberWithdrawnEvent가 발행된다")
	fun withdraw_publishesMemberWithdrawnEvent() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberService.withdraw(member.id.toString())

		assertThat(eventPublisher.events).anyMatch { it is MemberWithdrawnEvent }
	}

	@Test
	@DisplayName("존재하지 않는 ID로 withdraw 호출 시 MemberNotFoundException이 발생한다")
	fun withdraw_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberService.withdraw(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("logout 호출 시 TokenStore에서 refresh token을 삭제한다")
	fun logout_deletesRefreshTokenFromTokenStore() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))
		tokenStore.save(member.id.toString(), "some-refresh-token")

		memberService.logout(member.id.toString())

		assertThat(tokenStore.findMemberIdByRefreshToken("some-refresh-token")).isNull()
	}

	@Test
	@DisplayName("존재하지 않는 ID로 logout 호출 시 MemberNotFoundException이 발생한다")
	fun logout_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberService.logout(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	private class FakeMemberOutPort : MemberOutPort {
		private val store = linkedMapOf<MemberId, Member>()

		override fun save(member: Member): Member {
			store[member.id] = member
			return member
		}

		override fun findById(memberId: MemberId): Member? = store[memberId]

		override fun findByEmail(email: Email): Member? =
			store.values.firstOrNull { it.email == email }

		override fun existsByEmail(email: Email): Boolean = findByEmail(email) != null

		override fun findAllDeletedBefore(threshold: LocalDateTime): List<Member> =
			store.values.filter { it.deletedAt != null && it.deletedAt!!.isBefore(threshold) }
	}

	private class FakeSocialAccountOutPort : SocialAccountOutPort {
		private val store = linkedMapOf<Pair<SocialProvider, String>, SocialAccount>()

		override fun save(socialAccount: SocialAccount): SocialAccount {
			store[socialAccount.provider to socialAccount.providerId] = socialAccount
			return socialAccount
		}

		override fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount? =
			store[provider to providerId]
	}

	private class FakeTokenIssuer : TokenIssuer {
		override fun issue(memberId: String, role: Role): TokenPair =
			TokenPair("access-$memberId", "refresh-$memberId")
	}

	private class FakeTokenStore : TokenStore {
		private val memberToToken = mutableMapOf<String, String>()
		private val tokenToMember = mutableMapOf<String, String>()

		override fun save(memberId: String, refreshToken: String) {
			deleteByMemberId(memberId)
			memberToToken[memberId] = refreshToken
			tokenToMember[refreshToken] = memberId
		}

		override fun findMemberIdByRefreshToken(refreshToken: String): String? =
			tokenToMember[refreshToken]

		override fun deleteByMemberId(memberId: String) {
			memberToToken.remove(memberId)?.let { tokenToMember.remove(it) }
		}
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
