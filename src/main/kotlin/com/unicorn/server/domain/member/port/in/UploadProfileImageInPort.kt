package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand

// UploadProfileImageInPort - 멤버 프로필 이미지 업로드 유스케이스 진입점을 정의한다.
interface UploadProfileImageInPort {
	// 멤버 식별자와 업로드 명령으로 프로필 이미지를 갱신한다.
	fun uploadProfileImage(memberId: String, command: UploadProfileImageCommand): Member
}
