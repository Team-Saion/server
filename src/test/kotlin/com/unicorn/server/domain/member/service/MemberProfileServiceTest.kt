package com.unicorn.server.domain.member.service

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.port.out.storage.ObjectStorage
import com.unicorn.server.common.port.out.storage.ObjectUploadCommand
import com.unicorn.server.common.port.out.storage.StoredObject
import com.unicorn.server.common.port.out.storage.exception.ObjectSizeExceededException
import com.unicorn.server.common.port.out.storage.exception.UnsupportedContentTypeException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

@DisplayName("MemberProfileService 단위 테스트")
class MemberProfileServiceTest {

	private val memberOutPort = FakeMemberOutPort()
	private val eventPublisher = RecordingEventPublisher()
	private val objectStorage = FakeObjectStorage()
	private val memberProfileService = MemberProfileService(memberOutPort, eventPublisher, objectStorage)

	@Test
	@DisplayName("getById 호출 시 저장된 멤버를 반환한다")
	fun getById_returnsSavedMember() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		val result = memberProfileService.getById(member.id.toString())

		assertThat(result.id).isEqualTo(member.id)
	}

	@Test
	@DisplayName("존재하지 않는 ID로 getById 호출 시 MemberNotFoundException이 발생한다")
	fun getById_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberProfileService.getById(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("탈퇴한 멤버 ID로 getById 호출 시 WithdrawnMemberException이 발생한다")
	fun getById_whenWithdrawn_throwsWithdrawnMemberException() {
		val member = memberOutPort.save(Member.create(Email("withdrawn@example.com"), "홍길동", "길동이"))
		member.withdraw()
		memberOutPort.save(member)

		assertThatThrownBy { memberProfileService.getById(member.id.toString()) }
			.isInstanceOf(WithdrawnMemberException::class.java)
	}

	@Test
	@DisplayName("updateProfile 호출 시 닉네임이 변경된다")
	fun updateProfile_nicknameIsUpdated() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		val result = memberProfileService.updateProfile(member.id.toString(), UpdateProfileCommand("새닉네임"))

		assertThat(result.nickname).isEqualTo("새닉네임")
	}

	@Test
	@DisplayName("존재하지 않는 ID로 updateProfile 호출 시 MemberNotFoundException이 발생한다")
	fun updateProfile_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberProfileService.updateProfile(unknownId, UpdateProfileCommand("닉네임")) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("uploadProfileImage 호출 시 profileImageKey가 갱신된다")
	fun uploadProfileImage_success_updatesProfileImageKey() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		val result = memberProfileService.uploadProfileImage(member.id.toString(), uploadCommand("profile.png"))

		assertThat(result.profileImageKey).isNotNull()
	}

	@Test
	@DisplayName("기존 프로필 이미지가 있으면 업로드 후 기존 이미지를 삭제한다")
	fun uploadProfileImage_whenPreviousImageExists_deletesPreviousImage() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))
		val first = memberProfileService.uploadProfileImage(member.id.toString(), uploadCommand("first.png"))
		val previousKey = first.profileImageKey

		memberProfileService.uploadProfileImage(member.id.toString(), uploadCommand("second.png"))

		assertThat(objectStorage.deleted).contains(previousKey)
	}

	@Test
	@DisplayName("기존 프로필 이미지가 없으면 삭제를 호출하지 않는다")
	fun uploadProfileImage_whenNoPreviousImage_doesNotCallDelete() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberProfileService.uploadProfileImage(member.id.toString(), uploadCommand("first.png"))

		assertThat(objectStorage.deleted).isEmpty()
	}

	@Test
	@DisplayName("탈퇴한 멤버로 uploadProfileImage 호출 시 WithdrawnMemberException이 발생한다")
	fun uploadProfileImage_whenWithdrawn_throwsWithdrawnMemberException() {
		val member = memberOutPort.save(Member.create(Email("withdrawn2@example.com"), "홍길동", "길동이"))
		member.withdraw()
		memberOutPort.save(member)

		assertThatThrownBy {
			memberProfileService.uploadProfileImage(member.id.toString(), uploadCommand("profile.png"))
		}.isInstanceOf(WithdrawnMemberException::class.java)
	}

	@Test
	@DisplayName("허용되지 않은 contentType이면 UnsupportedContentTypeException이 발생한다")
	fun uploadProfileImage_withUnsupportedContentType_throwsException() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))
		val command = UploadProfileImageCommand("profile.gif", "image/gif", 100L, ByteArrayInputStream(ByteArray(0)))

		assertThatThrownBy { memberProfileService.uploadProfileImage(member.id.toString(), command) }
			.isInstanceOf(UnsupportedContentTypeException::class.java)
	}

	@Test
	@DisplayName("최대 용량을 초과하면 ObjectSizeExceededException이 발생한다")
	fun uploadProfileImage_withSizeExceeded_throwsException() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))
		val command = UploadProfileImageCommand(
			"profile.png",
			"image/png",
			20 * 1024 * 1024 + 1L,
			ByteArrayInputStream(ByteArray(0)),
		)

		assertThatThrownBy { memberProfileService.uploadProfileImage(member.id.toString(), command) }
			.isInstanceOf(ObjectSizeExceededException::class.java)
	}

	@Test
	@DisplayName("withdraw 호출 시 상태가 DELETED로 변경된다")
	fun withdraw_statusBecomesDeleted() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberProfileService.withdraw(member.id.toString())

		assertThat(memberOutPort.findById(member.id)!!.status).isEqualTo(MemberStatus.DELETED)
	}

	@Test
	@DisplayName("withdraw 호출 시 deletedAt이 세팅된다")
	fun withdraw_deletedAtIsSet() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberProfileService.withdraw(member.id.toString())

		assertThat(memberOutPort.findById(member.id)!!.deletedAt).isNotNull()
	}

	@Test
	@DisplayName("withdraw 호출 시 MemberWithdrawnEvent가 발행된다")
	fun withdraw_publishesMemberWithdrawnEvent() {
		val member = memberOutPort.save(Member.create(Email("test@example.com"), "홍길동", "길동이"))

		memberProfileService.withdraw(member.id.toString())

		assertThat(eventPublisher.events).anyMatch { it is MemberWithdrawnEvent }
	}

	@Test
	@DisplayName("존재하지 않는 ID로 withdraw 호출 시 MemberNotFoundException이 발생한다")
	fun withdraw_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = MemberId.generate().toString()

		assertThatThrownBy { memberProfileService.withdraw(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	private fun uploadCommand(filename: String): UploadProfileImageCommand =
		UploadProfileImageCommand(filename, "image/png", 100L, ByteArrayInputStream(ByteArray(0)))

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

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}

	private class FakeObjectStorage : ObjectStorage {
		val deleted = mutableListOf<String>()

		override fun upload(command: ObjectUploadCommand): StoredObject =
			StoredObject(
				command.objectKey,
				"https://example.com/${command.objectKey}",
				command.contentType,
				command.contentLength,
			)

		override fun delete(objectKey: String) {
			deleted += objectKey
		}

		override fun getUrl(objectKey: String): String = "https://example.com/$objectKey"
	}
}
