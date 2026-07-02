package com.unicorn.server.infrastructure.adapter.`in`.web.invitation

import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.port.dto.DispatchInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.`in`.AcceptCircleInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.DispatchInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.GetInvitationByTokenInPort
import com.unicorn.server.domain.invitation.port.`in`.IssueInvitationInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.AcceptInvitationResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.DispatchInvitationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.InvitationDetailResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssueInvitationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssuedInvitationResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/invitations")
class InvitationController(
	private val issueInvitationInPort: IssueInvitationInPort,
	private val dispatchInvitationInPort: DispatchInvitationInPort,
	private val getInvitationByTokenInPort: GetInvitationByTokenInPort,
	private val acceptCircleInvitationInPort: AcceptCircleInvitationInPort,
) : InvitationApiDoc {
	@PostMapping
	override fun issue(
		@AuthenticationPrincipal memberId: String,
		@RequestBody request: IssueInvitationRequest,
	): ApiResponse<IssuedInvitationResponse> = ApiResponse.created(
		IssuedInvitationResponse.from(
			issueInvitationInPort.issue(
				memberId,
				IssueInvitationCommand(
					type = InvitationType.valueOf(request.type),
					targetId = request.targetId,
					inviteToName = request.inviteToName,
					message = request.message,
				),
			),
		),
	)

	@PostMapping("/{invitationId}/dispatches")
	override fun dispatch(
		@PathVariable invitationId: String,
		@RequestBody request: DispatchInvitationRequest,
	): ApiResponse<Unit> {
		dispatchInvitationInPort.dispatch(
			DispatchInvitationCommand(invitationId, InvitationChannel.valueOf(request.channel)),
		)
		return ApiResponse.success()
	}

	@GetMapping("/by-token/{token}")
	override fun getByToken(@PathVariable token: String): ApiResponse<InvitationDetailResponse> =
		ApiResponse.success(InvitationDetailResponse.from(getInvitationByTokenInPort.getByToken(token)))

	@PostMapping("/by-token/{token}/accept")
	override fun accept(
		@PathVariable token: String,
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<AcceptInvitationResponse> =
		ApiResponse.success(AcceptInvitationResponse.from(acceptCircleInvitationInPort.accept(token, memberId)))
}
