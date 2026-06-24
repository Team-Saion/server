package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.domain.member.port.`in`.KakaoLoginInPort
import com.unicorn.server.domain.member.port.`in`.ReissueTokenInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.KakaoLoginRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.RefreshTokenRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.TokenResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// AuthController - 소셜 로그인 HTTP 요청을 수신하고 토큰을 발급한다.
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
	private val kakaoLoginInPort: KakaoLoginInPort,
	private val reissueTokenInPort: ReissueTokenInPort,
) : AuthApiDoc {
	@PostMapping("/kakao")
	override fun kakaoLogin(@RequestBody @Valid request: KakaoLoginRequest): ApiResponse<TokenResponse> {
		val tokenPair = kakaoLoginInPort.kakaoLogin(request.idToken)
		return ApiResponse.success(TokenResponse.from(tokenPair))
	}

	@PostMapping("/refresh")
	override fun reissue(@RequestBody @Valid request: RefreshTokenRequest): ApiResponse<TokenResponse> {
		val tokenPair = reissueTokenInPort.reissue(request.refreshToken)
		return ApiResponse.success(TokenResponse.from(tokenPair))
	}
}
