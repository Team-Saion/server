package com.unicorn.server.domain.member.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.port.out.storage.ObjectStorage
import com.unicorn.server.common.port.out.storage.ObjectType
import com.unicorn.server.common.port.out.storage.ObjectUploadCommand
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.`in`.GetMemberInPort
import com.unicorn.server.domain.member.port.`in`.UpdateProfileInPort
import com.unicorn.server.domain.member.port.`in`.UploadProfileImageInPort
import com.unicorn.server.domain.member.port.`in`.WithdrawMemberInPort
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.vo.MemberId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// MemberProfileService - 멤버 조회, 프로필/프로필 이미지 변경, 회원탈퇴 유스케이스를 처리한다.
@Service
@Transactional(readOnly = true)
class MemberProfileService(
	private val memberOutPort: MemberOutPort,
	private val eventPublisher: EventPublisher,
	private val objectStorage: ObjectStorage,
) : GetMemberInPort, UpdateProfileInPort, WithdrawMemberInPort, UploadProfileImageInPort {

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

	// 프로필 이미지를 업로드하고 기존 이미지를 정리한다.
	@Transactional
	override fun uploadProfileImage(memberId: String, command: UploadProfileImageCommand): Member {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 업로드 정책 검증 및 스토리지 업로드
		ObjectType.PROFILE_IMAGE.validate(command.contentType, command.contentLength)
		val objectKey = ObjectType.PROFILE_IMAGE.generateObjectKey(command.originalFilename)
		val stored = objectStorage.upload(
			ObjectUploadCommand(objectKey, command.contentType, command.contentLength, command.inputStream),
		)

		// 도메인 상태 변경 및 저장
		val previousImageKey = member.profileImageKey
		member.changeProfileImage(stored.objectKey)
		val savedMember = memberOutPort.save(member)

		// 기존 이미지 정리 (best-effort, 실패해도 업로드 응답은 성공 유지)
		previousImageKey?.let { deleteQuietly(it) }

		return savedMember
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

		// 탈퇴 이벤트 발행
		eventPublisher.publish(MemberWithdrawnEvent(savedMember.id.toString()))
	}

	// 멤버 식별자로 도메인을 조회하고 없으면 도메인 예외를 던진다.
	private fun findMemberOrThrow(memberId: String): Member {
		val member = memberOutPort.findById(MemberId.of(memberId)) ?: throw MemberNotFoundException(memberId)
		if (member.isDeleted()) {
			throw WithdrawnMemberException(memberId)
		}
		return member
	}

	// 새 이미지 저장 성공 후 기존 이미지를 삭제한다. 실패해도 업로드 응답은 성공으로 유지한다.
	private fun deleteQuietly(objectKey: String) {
		runCatching { objectStorage.delete(objectKey) }
			.onFailure { e -> log.warn("Failed to delete stale profile image - objectKey={}", objectKey, e) }
	}

	companion object {
		private val log = LoggerFactory.getLogger(MemberProfileService::class.java)
	}
}
