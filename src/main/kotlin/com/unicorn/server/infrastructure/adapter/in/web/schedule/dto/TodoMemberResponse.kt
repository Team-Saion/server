package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.port.dto.TodoMemberResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "할일 담당자 응답")
data class TodoMemberResponse(
	@field:Schema(description = "담당자 멤버 ID", example = "00000000-0000-0000-0000-000000000001")
	val id: String,

	@field:Schema(description = "담당자 닉네임", example = "유니콘")
	val nickname: String,

	@field:Schema(description = "담당자 아바타 색상 코드", example = "TEAL_200")
	val avatarColor: String,

	@field:Schema(description = "담당자의 할일 체크 여부", example = "false")
	val checked: Boolean,
) {
	companion object {
		fun from(result: TodoMemberResult): TodoMemberResponse = TodoMemberResponse(
			id = result.memberId,
			nickname = result.nickname,
			avatarColor = result.avatarColor.code,
			checked = result.checked,
		)
	}
}
