package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.domain.member.port.`in`.MemberAppleLoginInPort
import com.unicorn.server.domain.member.port.`in`.MemberKakaoLoginInPort
import com.unicorn.server.domain.member.port.`in`.MemberTokenReissueInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.AppleLoginRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.KakaoLoginRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.RefreshTokenRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.SocialLoginResponse
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
	private val kakaoLoginInPort: MemberKakaoLoginInPort,
	private val appleLoginInPort: MemberAppleLoginInPort,
	private val reissueTokenInPort: MemberTokenReissueInPort,
) : AuthApiDoc {

	@PostMapping("/kakao")
	override fun kakaoLogin(@RequestBody @Valid request: KakaoLoginRequest): ApiResponse<SocialLoginResponse> {
		val result = kakaoLoginInPort.kakaoLogin(request.idToken)
		return ApiResponse.success(SocialLoginResponse.from(result))
	}

	@PostMapping("/apple")
	override fun appleLogin(@RequestBody @Valid request: AppleLoginRequest): ApiResponse<SocialLoginResponse> {
		val result = appleLoginInPort.appleLogin(request.idToken)
		return ApiResponse.success(SocialLoginResponse.from(result))
	}

	@PostMapping("/refresh")
	override fun reissue(@RequestBody @Valid request: RefreshTokenRequest): ApiResponse<TokenResponse> {
		val tokenPair = reissueTokenInPort.reissue(request.refreshToken)
		return ApiResponse.success(TokenResponse.from(tokenPair))
	}
}
