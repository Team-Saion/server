package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Schema(description = "할일 담당자 배정 요청")
data class AssignTodoMembersRequest(
	@field:Schema(
		description = """
			담당자로 배정할 멤버 ID 목록입니다.
			기존 담당자 목록을 이 목록으로 전체 교체합니다. 빈 배열이면 담당자가 없는 할일이 됩니다.
		""",
		example = "[\"00000000-0000-0000-0000-000000000001\"]",
	)
	@field:NotNull
	val assigneeMemberIds: List<String>,
)
