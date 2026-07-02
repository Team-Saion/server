package com.unicorn.server.infrastructure.adapter.`in`.web.invitation

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.AcceptInvitationResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.DispatchInvitationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.InvitationDetailResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssueInvitationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssuedInvitationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Invitation API", description = "초대장 발급, 조회, 발송 로그, 수락 API")
interface InvitationApiDoc {
	@Operation(
		summary = "초대장 발급",
		description = """
			인증된 구성원이 써클 초대장을 발급합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 현재는 type=CIRCLE만 지원합니다.
			- 발급자는 대상 써클의 활성 구성원이어야 합니다.
			- 초대장은 48시간 동안 유효합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_TARGET_INVALID"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_NOT_AUTHORIZED"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITE_TO_NAME_INVALID"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITE_MESSAGE_INVALID"),
	)
	@ApiSuccessCodeExample(IssuedInvitationResponse::class)
	fun issue(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: IssueInvitationRequest,
	): ApiResponse<IssuedInvitationResponse>

	@Operation(
		summary = "초대장 발송 채널 로깅",
		description = """
			이미 발급된 초대장을 어떤 채널로 공유했는지 기록합니다.

			- 링크 복사 / 메시지 전송 / 카카오톡 전송을 각각 로깅합니다.
			- 현재 응답은 200 OK입니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun dispatch(
		@PathVariable invitationId: String,
		@RequestBody @Valid request: DispatchInvitationRequest,
	): ApiResponse<Unit>

	@Operation(
		summary = "초대장 수락 화면 조회",
		description = """
			초대 링크로 진입했을 때 수락 화면에 필요한 정보를 조회합니다.

			- 공개 API입니다. 인증이 필요 없습니다.
			- 이 조회가 곧 '초대 링크 클릭'으로 간주되어 클릭 로그가 기록됩니다.
			- 만료된 초대장은 410을 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_NOT_FOUND"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_EXPIRED"),
	)
	@ApiSuccessCodeExample(InvitationDetailResponse::class)
	fun getByToken(
		@PathVariable token: String,
	): ApiResponse<InvitationDetailResponse>

	@Operation(
		summary = "초대장 수락",
		description = """
			초대 링크를 수락해 써클에 참여합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 자기 자신이 발급한 초대장은 수락할 수 없습니다.
			- 이미 참여 중인 사용자가 다시 수락하면 `alreadyJoined=true`로 성공 응답합니다.
			- 같은 링크는 만료 전까지 여러 사용자가 순차 수락할 수 있습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_NOT_FOUND"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_EXPIRED"),
		ApiErrorCodeExample(codeType = InvitationErrorCode::class, code = "INVITATION_SELF_APPROVAL_FORBIDDEN"),
	)
	@ApiSuccessCodeExample(AcceptInvitationResponse::class)
	fun accept(
		@PathVariable token: String,
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<AcceptInvitationResponse>
}
