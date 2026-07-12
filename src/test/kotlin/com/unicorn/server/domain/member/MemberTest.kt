package com.unicorn.server.domain.member

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.exception.MemberErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Member 도메인 단위 테스트")
class MemberTest {

	@Test
	@DisplayName("Member 생성 시 초기 상태는 ACTIVE이다")
	fun create_initialStatusIsActive() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThat(member.status).isEqualTo(MemberStatus.ACTIVE)
	}

	@Test
	@DisplayName("Member 생성 시 기본 역할은 MEMBER이다")
	fun create_defaultRoleIsMember() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThat(member.role).isEqualTo(Role.MEMBER)
	}

	@Test
	@DisplayName("Member 생성 시 profileImageKey는 null이다")
	fun create_profileImageKeyIsNull() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThat(member.profileImageKey).isNull()
	}

	@Test
	@DisplayName("Member 생성 시 avatarColor가 할당된다")
	fun create_avatarColorIsAssigned() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThat(member.avatarColor).isIn(*AvatarColor.values())
	}

	@Test
	@DisplayName("withdraw 호출 시 상태가 DELETED가 된다")
	fun withdraw_statusBecomesDeleted() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.withdraw()

		assertThat(member.status).isEqualTo(MemberStatus.DELETED)
	}

	@Test
	@DisplayName("withdraw 호출 시 deletedAt이 세팅된다")
	fun withdraw_deletedAtIsSet() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.withdraw()

		assertThat(member.deletedAt).isNotNull()
	}

	@Test
	@DisplayName("이미 탈퇴한 멤버를 다시 withdraw하면 예외가 발생한다")
	fun withdraw_whenAlreadyDeleted_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")
		member.withdraw()

		assertThatThrownBy { member.withdraw() }
			.isInstanceOf(BusinessException::class.java)
	}

	@Test
	@DisplayName("updateProfile 호출 시 닉네임이 변경된다")
	fun updateProfile_nicknameIsUpdated() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.updateProfile("새닉네임")

		assertThat(member.nickname).isEqualTo("새닉네임")
	}

	@Test
	@DisplayName("닉네임이 2자 미만이면 예외가 발생한다")
	fun updateProfile_withTooShortNickname_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThatThrownBy { member.updateProfile("a") }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_NICKNAME)
	}

	@Test
	@DisplayName("닉네임이 10자 초과이면 예외가 발생한다")
	fun updateProfile_withTooLongNickname_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThatThrownBy { member.updateProfile("a".repeat(11)) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_NICKNAME)
	}

	@Test
	@DisplayName("닉네임 앞뒤에 공백이 있으면 예외가 발생한다")
	fun updateProfile_withLeadingOrTrailingWhitespace_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThatThrownBy { member.updateProfile(" 새닉네임 ") }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_NICKNAME)
	}

	@Test
	@DisplayName("PENDING 상태가 아닌 멤버가 completeOnboarding을 호출하면 예외가 발생한다")
	fun completeOnboarding_whenAlreadyOnboarded_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이", Role.MEMBER)

		assertThatThrownBy { member.completeOnboarding("새닉네임") }
			.isInstanceOf(IllegalStateException::class.java)
			.hasMessageContaining("Member is already onboarded")
	}

	@Test
	@DisplayName("changeProfileImage 호출 시 profileImageKey가 변경된다")
	fun changeProfileImage_keyIsUpdated() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.changeProfileImage("images/profile/new-key.png")

		assertThat(member.profileImageKey).isEqualTo("images/profile/new-key.png")
	}

	@Test
	@DisplayName("빈 objectKey로 changeProfileImage 호출 시 예외가 발생한다")
	fun changeProfileImage_withBlankKey_throwsException() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThatThrownBy { member.changeProfileImage(" ") }
			.isInstanceOf(IllegalArgumentException::class.java)
	}

	@Test
	@DisplayName("잘못된 이메일 형식으로 Email 생성 시 예외가 발생한다")
	fun email_withInvalidFormat_throwsException() {
		assertThatThrownBy { Email("not-an-email") }
			.isInstanceOf(IllegalArgumentException::class.java)
	}

	@Test
	@DisplayName("앞뒤 공백이 포함된 이메일로 Email 생성 시 예외가 발생한다")
	fun email_withLeadingOrTrailingWhitespace_throwsException() {
		assertThatThrownBy { Email(" test@example.com ") }
			.isInstanceOf(IllegalArgumentException::class.java)
	}

	@Test
	@DisplayName("isDeleted는 DELETED 상태일 때만 true를 반환한다")
	fun isDeleted_returnsTrueOnlyWhenDeleted() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		assertThat(member.isDeleted()).isFalse()

		member.withdraw()

		assertThat(member.isDeleted()).isTrue()
	}

	@Test
	@DisplayName("withdraw 호출 시 원본 이메일을 반환한다")
	fun withdraw_returnsOriginalEmail() {
		val originalEmail = Email("test@example.com")
		val member = Member.create(originalEmail, "홍길동", "길동이")

		val returned = member.withdraw()

		assertThat(returned).isEqualTo(originalEmail)
	}

	@Test
	@DisplayName("withdraw 호출 후 email이 마스킹 패턴으로 변경된다")
	fun withdraw_masksEmail() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.withdraw()

		assertThat(member.email?.value).matches("deleted_.+@deleted\\.saion")
	}

	@Test
	@DisplayName("withdraw 호출 후 nickname이 del로 시작하는 9자 이하 영숫자 패턴으로 마스킹된다")
	fun withdraw_masksNickname() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.withdraw()

		assertThat(member.nickname).matches("del[a-zA-Z0-9]{6}")
	}

	@Test
	@DisplayName("withdraw 호출 후 name과 nickname이 같은 마스킹 값으로 변경된다")
	fun withdraw_masksNameAndNickname() {
		val member = Member.create(Email("test@example.com"), "홍길동", "길동이")

		member.withdraw()

		val expected = "del${member.id.toString().replace("-", "").take(6)}"
		assertThat(member.name).isEqualTo(expected)
		assertThat(member.nickname).isEqualTo(expected)
	}

	@Test
	@DisplayName("email이 null인 멤버를 withdraw하면 null을 반환하고 email은 여전히 null이다")
	fun withdraw_whenEmailIsNull_returnsNull() {
		val member = Member.create(null, "홍길동", "길동이")

		val returned = member.withdraw()

		assertThat(returned).isNull()
		assertThat(member.email).isNull()
	}

	@Test
	@DisplayName("name이 null인 멤버를 withdraw하면 name은 여전히 null이고 nickname은 마스킹된다")
	fun withdraw_whenNameIsNull_keepsNameNullAndMasksNickname() {
		val member = Member.create(Email("test@example.com"), null, "길동이")

		member.withdraw()

		assertThat(member.name).isNull()
		assertThat(member.nickname).isEqualTo("del${member.id.toString().replace("-", "").take(6)}")
	}
}
