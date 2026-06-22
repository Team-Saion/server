package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.domain.member.port.`in`.GetMemberInPort
import com.unicorn.server.domain.member.port.`in`.LogoutInPort
import com.unicorn.server.domain.member.port.`in`.UpdateProfileInPort
import com.unicorn.server.domain.member.port.`in`.UploadProfileImageInPort
import com.unicorn.server.domain.member.port.`in`.WithdrawMemberInPort
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.MemberResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.UpdateProfileRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

// MemberController - 인증된 멤버의 프로필 조회/변경, 프로필 이미지 업로드, 로그아웃, 회원탈퇴 엔드포인트를 처리한다.
@RestController
@RequestMapping("/v1/members")
class MemberController(
	private val getMemberInPort: GetMemberInPort,
	private val updateProfileInPort: UpdateProfileInPort,
	private val uploadProfileImageInPort: UploadProfileImageInPort,
	private val logoutInPort: LogoutInPort,
	private val withdrawMemberInPort: WithdrawMemberInPort,
) : MemberApiDoc {

	// GET /api/v1/members/me - 내 프로필을 조회한다.
	@GetMapping("/me")
	override fun getMyProfile(
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<MemberResponse> {
		val member = getMemberInPort.getById(memberId)
		return ApiResponse.success(MemberResponse.from(member))
	}

	// PATCH /api/v1/members/me/profile - 닉네임을 변경한다.
	@PatchMapping("/me/profile")
	override fun updateProfile(
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: UpdateProfileRequest,
	): ApiResponse<MemberResponse> {
		val member = updateProfileInPort.updateProfile(memberId, UpdateProfileCommand(request.nickname))
		return ApiResponse.success(MemberResponse.from(member))
	}

	// POST /api/v1/members/me/profile-image - 프로필 이미지를 업로드한다.
	@PostMapping("/me/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
	override fun uploadProfileImage(
		@AuthenticationPrincipal memberId: String,
		@RequestPart("image") image: MultipartFile,
	): ApiResponse<MemberResponse> {
		val command = UploadProfileImageCommand(
			originalFilename = image.originalFilename ?: "",
			contentType = image.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE,
			contentLength = image.size,
			inputStream = image.inputStream,
		)
		val member = uploadProfileImageInPort.uploadProfileImage(memberId, command)
		return ApiResponse.success(MemberResponse.from(member))
	}

	// POST /api/v1/members/me/logout - 리프레시 토큰을 무효화하고 로그아웃한다.
	@PostMapping("/me/logout")
	override fun logout(
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit> {
		logoutInPort.logout(memberId)
		return ApiResponse.success()
	}

	// DELETE /api/v1/members/me - 계정을 소프트 삭제한다.
	@DeleteMapping("/me")
	override fun withdraw(
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit> {
		withdrawMemberInPort.withdraw(memberId)
		return ApiResponse.success()
	}
}
