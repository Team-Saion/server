package com.unicorn.server.domain.member.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.port.out.storage.ObjectStorage
import com.unicorn.server.common.port.out.storage.ObjectType
import com.unicorn.server.common.port.out.storage.ObjectUploadCommand
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.WithdrawalLog
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.`in`.GetMemberInPort
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.port.`in`.GetOnboardingInfoInPort
import com.unicorn.server.domain.member.port.`in`.UpdateMemberStateInPort
import com.unicorn.server.domain.member.port.`in`.UpdateProfileInPort
import com.unicorn.server.domain.member.port.`in`.UploadProfileImageInPort
import com.unicorn.server.domain.member.port.`in`.WithdrawMemberInPort
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.WithdrawalLogOutPort
import com.unicorn.server.domain.member.vo.MemberId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

// MemberProfileService - 멤버 조회, 프로필/프로필 이미지 변경, 회원탈퇴 유스케이스를 처리한다.
@Service
@Transactional(readOnly = true)
class MemberProfileService(
	private val memberOutPort: MemberOutPort,
	private val socialAccountOutPort: SocialAccountOutPort,
	private val eventPublisher: EventPublisher,
	private val objectStorage: ObjectStorage,
	private val withdrawalLogOutPort: WithdrawalLogOutPort,
) : GetMemberInPort, GetMemberProfileInPort, GetOnboardingInfoInPort, UpdateProfileInPort, WithdrawMemberInPort, UploadProfileImageInPort, UpdateMemberStateInPort {

	// 멤버 식별자로 저장된 멤버를 조회한다.
	override fun getById(memberId: String): Member = findMemberOrThrow(memberId)

	override fun getMemberProfile(memberId: String): MemberProfileDto? {
		val member = memberOutPort.findById(MemberId.of(memberId)) ?: return null
		val socialAccount = socialAccountOutPort.findByMemberId(member.id)
		return MemberProfileDto(
			memberId = member.id.toString(),
			nickname = member.nickname,
			avatarColor = member.avatarColor.name,
			kakaoNickname = socialAccount?.kakaoNickname,
			active = !member.isDeleted(),
		)
	}

	// 온보딩 화면 표시를 위한 소셜 계정 정보와 멤버 기본 색상을 조회한다.
	override fun getOnboardingInfo(memberId: String): OnboardingInfoResult {
		val member = findMemberOrThrow(memberId)
		val socialAccount = socialAccountOutPort.findByMemberId(member.id)
		return OnboardingInfoResult(
			socialNickname = socialAccount?.kakaoNickname,
			socialProfileImageUrl = socialAccount?.kakaoProfileImageUrl,
			avatarColor = member.avatarColor,
		)
	}

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
		val stored = command.inputStream.use { inputStream ->
			objectStorage.upload(
				ObjectUploadCommand(objectKey, command.contentType, command.contentLength, inputStream),
			)
		}

		// 도메인 상태 변경 및 저장
		val previousImageKey = member.profileImageKey
		member.changeProfileImage(stored.objectKey)
		val savedMember = memberOutPort.save(member)

		// 기존 이미지 정리 (best-effort, 실패해도 업로드 응답은 성공 유지)
		previousImageKey?.let { deleteAfterCommit(it) }

		return savedMember
	}

	// 멤버를 soft delete 처리한다.
	@Transactional
	override fun withdraw(memberId: String, reason: String) {
		// 데이터 조회
		val member = findMemberOrThrow(memberId)

		// 도메인 상태 변경
		val originalEmail = member.withdraw()

		// 변경 데이터 저장
		val savedMember = memberOutPort.save(member)

		// 탈퇴 로그 저장
		withdrawalLogOutPort.save(
			WithdrawalLog.create(
				memberId = savedMember.id,
				originalEmail = originalEmail?.value,
				reason = reason,
				withdrawnAt = requireNotNull(savedMember.deletedAt) { "deletedAt must not be null after withdrawal" },
			),
		)

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
	private fun deleteAfterCommit(objectKey: String) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			deleteQuietly(objectKey)
			return
		}

		TransactionSynchronizationManager.registerSynchronization(
			object : TransactionSynchronization {
				override fun afterCommit() {
					deleteQuietly(objectKey)
				}
			},
		)
	}

	private fun deleteQuietly(objectKey: String) {
		runCatching { objectStorage.delete(objectKey) }
			.onFailure { e -> log.warn("Failed to delete stale profile image - objectKey={}", objectKey, e) }
	}

	// 멤버 상태/역할을 강제로 변경한다. 탈퇴 상태에서도 재활성화할 수 있도록 탈퇴 가드를 우회해 직접 조회한다.
	@Transactional
	override fun updateMemberState(memberId: String, status: MemberStatus?, role: Role?): Member {
		val member = memberOutPort.findById(MemberId.of(memberId)) ?: throw MemberNotFoundException(memberId)

		status?.let { member.changeStatus(it) }
		role?.let { member.changeRole(it) }

		return memberOutPort.save(member)
	}

	companion object {
		private val log = LoggerFactory.getLogger(MemberProfileService::class.java)
	}
}
