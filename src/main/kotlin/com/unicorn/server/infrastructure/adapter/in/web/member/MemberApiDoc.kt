package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.common.port.out.storage.exception.ObjectStorageErrorCode
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.CompleteOnboardingRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.MemberResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.OnboardingInfoResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.TokenResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.UpdateProfileRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Member API", description = "인증된 멤버의 온보딩, 프로필 조회/변경, 로그아웃, 회원탈퇴 API")
interface MemberApiDoc {

	@Operation(
		summary = "내 프로필 조회",
		description = """
			현재 인증된 멤버의 프로필 정보를 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- MEMBER, ADMIN 역할만 접근할 수 있습니다. PENDING 역할은 403을 반환합니다.
			- 탈퇴한 멤버는 접근할 수 없습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(MemberResponse::class)
	fun getMyProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<MemberResponse>

	@Operation(
		summary = "온보딩 사전정보 조회",
		description = """
			온보딩 화면에 필요한 카카오 프로필 정보와 멤버 아바타 색상을 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- PENDING, MEMBER 역할 모두 접근할 수 있습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(OnboardingInfoResponse::class)
	fun getOnboardingInfo(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<OnboardingInfoResponse>

	@Operation(
		summary = "온보딩 완료",
		description = """
			온보딩에서 입력한 닉네임을 저장하고 PENDING 역할을 MEMBER로 전환한 뒤 새 토큰을 발급합니다.

			- 요청 바디: `nickname`
			- 닉네임은 앞뒤 공백 제거 후 2자 이상 10자 이하입니다.
			- 닉네임은 한글, 영문, 숫자만 허용합니다.
			- PENDING, MEMBER 역할 모두 접근할 수 있습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "INVALID_NICKNAME"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(TokenResponse::class)
	fun completeOnboarding(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: CompleteOnboardingRequest,
	): ApiResponse<TokenResponse>

	@Operation(
		summary = "내 프로필 수정",
		description = """
			현재 인증된 멤버의 닉네임을 변경합니다.

			- 요청 바디: `nickname`
			- MEMBER, ADMIN 역할만 접근할 수 있습니다. PENDING 역할은 403을 반환합니다.
			- 닉네임은 2자 이상 10자 이하입니다.
			- 닉네임은 한글, 영문, 숫자만 허용합니다.
			- 앞뒤 공백이 포함된 닉네임은 허용하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "INVALID_NICKNAME"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(MemberResponse::class)
	fun updateProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: UpdateProfileRequest,
	): ApiResponse<MemberResponse>

	@Operation(
		summary = "내 프로필 이미지 업로드",
		description = """
			현재 인증된 멤버의 프로필 이미지를 업로드합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- PENDING, MEMBER, ADMIN 역할 모두 접근할 수 있습니다.
			- multipart/form-data 요청이며 파일은 `image` 파트로 전송합니다.
			- 허용 포맷: image/jpeg, image/png, image/webp
			- 최대 용량: 20MB
			- 기존에 등록된 이미지가 있으면 새 이미지로 교체된 뒤 기존 이미지는 스토리지에서 삭제됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
		ApiErrorCodeExample(codeType = ObjectStorageErrorCode::class, code = "UNSUPPORTED_CONTENT_TYPE"),
		ApiErrorCodeExample(codeType = ObjectStorageErrorCode::class, code = "OBJECT_SIZE_EXCEEDED"),
		ApiErrorCodeExample(codeType = ObjectStorageErrorCode::class, code = "UPLOAD_FAILED"),
	)
	@ApiSuccessCodeExample(MemberResponse::class)
	fun uploadProfileImage(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "업로드할 프로필 이미지 파일 (jpeg/png/webp, 최대 20MB)")
		@RequestPart("image") image: MultipartFile,
	): ApiResponse<MemberResponse>

	@Operation(
		summary = "로그아웃",
		description = """
			현재 인증된 멤버의 refresh token을 무효화합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- MEMBER, ADMIN 역할만 접근할 수 있습니다. PENDING 역할은 403을 반환합니다.
			- access token 자체는 stateless JWT이므로 서버 저장소에서 삭제하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun logout(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "회원 탈퇴",
		description = """
			현재 인증된 멤버를 소프트 삭제 처리합니다.

			- MEMBER, ADMIN 역할만 접근할 수 있습니다. PENDING 역할은 403을 반환합니다.
			- 복구 정책과 보관 기간을 고려해 즉시 물리 삭제하지 않습니다.
			- 탈퇴 시 refresh token도 무효화합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun withdraw(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit>
}
