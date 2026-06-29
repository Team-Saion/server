package com.unicorn.server.domain.member.service

import com.unicorn.server.BaseTest
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.adapter.out.token.InMemoryTokenStoreAdapter
import com.unicorn.server.infrastructure.adapter.out.token.JwtProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("Member seed 토큰 테스트")
class MemberSeedTest : BaseTest() {

    private val memberOutPort = FakeMemberOutPort()
    private val socialAccountOutPort = FakeSocialAccountOutPort()
    private val tokenStore: TokenStore = InMemoryTokenStoreAdapter()
    private val jwtProvider = JwtProvider(testJwtSecret, accessTokenExpirationSeconds, refreshTokenExpirationSeconds)
    private val memberAuthService = MemberAuthService(memberOutPort, socialAccountOutPort, jwtProvider, tokenStore)

    @Test
    @DisplayName("baseMember 정보로 access token을 발급하고 로그에 출력한다")
    fun issueAccessToken_withBaseMember_printsAccessToken() {
        val seededMember = baseMember.toDomain()
        memberOutPort.save(seededMember)
        socialAccountOutPort.save(
            SocialAccount.create(
                memberId = seededMember.id,
                provider = SocialProvider.KAKAO,
                providerId = baseMember.providerId,
                email = baseMember.email,
                kakaoNickname = baseMember.nickname,
                kakaoProfileImageUrl = null,
            ),
        )

        val result = memberAuthService.login(
            SocialLoginCommand(
                provider = SocialProvider.KAKAO,
                providerId = baseMember.providerId,
                email = baseMember.email,
                name = baseMember.name,
            ),
        )

        println("[MemberSeedTest] accessToken=${result.tokenPair.accessToken}")

        assertThat(result.tokenPair.accessToken).isNotBlank()
        assertThat(jwtProvider.validate(result.tokenPair.accessToken)).isTrue()
        assertThat(jwtProvider.extractMemberId(result.tokenPair.accessToken)).isEqualTo(baseMember.id)
        assertThat(jwtProvider.extractRoles(result.tokenPair.accessToken)).containsExactly(Role.MEMBER.name)
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

        override fun findByMemberId(memberId: MemberId): SocialAccount? =
            store.values.firstOrNull { it.memberId == memberId }
    }
}
