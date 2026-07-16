package com.unicorn.server.infrastructure.adapter.`in`.web.home

import com.unicorn.server.domain.home.port.`in`.HomeQueryInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleHomeResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleMemberResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/homes")
class HomeController(
	private val homeQueryInPort: HomeQueryInPort,
	@param:Value($$"${app.server.url}") private val serverUrl: String,
) : HomeApiDoc {
	@GetMapping("/{circleId}")
	override fun getHome(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
	): ApiResponse<CircleHomeResponse> =
		ApiResponse.success(CircleHomeResponse.from(homeQueryInPort.getHome(circleId, memberId), serverUrl))

	@GetMapping("/{circleId}/members")
	override fun getMembers(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
	): ApiResponse<List<CircleMemberResponse>> =
		ApiResponse.success(homeQueryInPort.getMembers(circleId, memberId).map { CircleMemberResponse.from(it, serverUrl) })
}
