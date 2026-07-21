package com.unicorn.server.infrastructure.adapter.`in`.web.invitation

import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.`in`.AcceptCircleInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.GetInvitationByTokenInPort
import com.unicorn.server.domain.invitation.port.`in`.IssueInvitationInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.AcceptInvitationResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.InvitationDetailResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssueInvitationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto.IssuedInvitationResponse
import jakarta.validation.Valid
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
    private val getInvitationByTokenInPort: GetInvitationByTokenInPort,
    private val acceptCircleInvitationInPort: AcceptCircleInvitationInPort,
) : InvitationApiDoc {
    @PostMapping
    override fun issue(
        @AuthenticationPrincipal memberId: String,
        @RequestBody @Valid request: IssueInvitationRequest,
    ): ApiResponse<IssuedInvitationResponse> =
        ApiResponse.created(
            IssuedInvitationResponse.from(
                issueInvitationInPort.issue(
                    memberId,
                    IssueInvitationCommand(targetId = request.targetId),
                ),
            ),
        )

    @GetMapping("/{token}")
    override fun getByToken(@PathVariable token: String): ApiResponse<InvitationDetailResponse> =
        ApiResponse.success(InvitationDetailResponse.from(getInvitationByTokenInPort.getByToken(token)))

    @PostMapping("/{token}")
    override fun accept(
        @PathVariable token: String,
        @AuthenticationPrincipal memberId: String,
    ): ApiResponse<AcceptInvitationResponse> =
        ApiResponse.success(AcceptInvitationResponse.from(acceptCircleInvitationInPort.accept(token, memberId)))
}
