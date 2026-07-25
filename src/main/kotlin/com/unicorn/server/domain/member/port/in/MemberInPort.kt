package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand

// MemberInPort - 인증된 본인이 자신의 멤버 정보를 조회/변경하는 유스케이스 진입점을 정의한다.
interface MemberInPort {
	// 멤버 식별자로 멤버를 조회한다.
	fun getById(memberId: String): Member

	// 온보딩 화면 표시를 위한 소셜 계정 정보와 멤버 기본 색상을 조회한다.
	fun getOnboardingInfo(memberId: String): OnboardingInfoResult

	// 멤버 식별자와 변경 명령으로 프로필을 갱신한다.
	fun updateProfile(memberId: String, command: UpdateProfileCommand): Member

	// 멤버 식별자와 업로드 명령으로 프로필 이미지를 갱신한다.
	fun uploadProfileImage(memberId: String, command: UploadProfileImageCommand): Member

	// 멤버를 soft delete 상태로 전환한다.
	fun withdraw(memberId: String, reason: String)
}
