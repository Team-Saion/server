package com.unicorn.server.infrastructure.security

import com.unicorn.server.infrastructure.adapter.out.token.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// JwtAuthenticationFilter - 요청마다 Bearer 토큰을 검증하고 SecurityContext를 채운다.
@Component
class JwtAuthenticationFilter(
	private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		extractToken(request)
			?.takeIf { jwtProvider.validate(it) }
			?.let { token ->
				val memberId = jwtProvider.extractMemberId(token) ?: return@let
				val authorities = jwtProvider.extractRoles(token)
					.map { role -> SimpleGrantedAuthority("ROLE_$role") }
				val auth = UsernamePasswordAuthenticationToken(memberId, null, authorities)
				SecurityContextHolder.getContext().authentication = auth
			}

		filterChain.doFilter(request, response)
	}

	// Authorization header에서 Bearer token만 추출한다.
	private fun extractToken(request: HttpServletRequest): String? {
		val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
		if (!header.startsWith("Bearer ")) return null
		return header.removePrefix("Bearer ")
	}
}
