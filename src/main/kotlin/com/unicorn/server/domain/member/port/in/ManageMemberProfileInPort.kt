package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand
import com.unicorn.server.domain.member.port.dto.UploadProfileImageCommand

// ManageMemberProfileInPort - 멤버 프로필 정보와 이미지를 변경하는 유스케이스 진입점을 정의한다.
interface ManageMemberProfileInPort {
	fun updateProfile(memberId: String, command: UpdateProfileCommand): Member

	fun uploadProfileImage(memberId: String, command: UploadProfileImageCommand): Member
}
