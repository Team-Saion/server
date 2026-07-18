package com.unicorn.server.infrastructure.adapter.`in`.web.circle

import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleSummaryResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CreateCircleRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleTransferInitiatorRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/circles")
class CircleController(
	private val circleInPort: CircleInPort,
	private val circleMemberInPort: CircleMemberInPort,
) : CircleApiDoc {
	@GetMapping
	override fun listCircles(
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<List<CircleSummaryResponse>> =
		ApiResponse.success(circleInPort.listCircles(memberId).map(CircleSummaryResponse::from))

	@PostMapping
	override fun create(
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: CreateCircleRequest,
	): ApiResponse<CircleSummaryResponse> =
		ApiResponse.created(CircleSummaryResponse.from(circleInPort.create(memberId, CreateCircleCommand(request.name))))

	@DeleteMapping("/{circleId}/members/me")
	override fun leave(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
	): ApiResponse<Unit> {
		circleMemberInPort.leave(circleId, memberId)
		return ApiResponse.success()
	}

	@PatchMapping("/{circleId}/initiator")
	override fun transferInitiator(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@RequestBody @Valid request: CircleTransferInitiatorRequest,
	): ApiResponse<CircleSummaryResponse> =
		ApiResponse.success(
			CircleSummaryResponse.from(
				circleMemberInPort.transferInitiator(circleId, memberId, request.targetMemberId),
			),
		)
}
