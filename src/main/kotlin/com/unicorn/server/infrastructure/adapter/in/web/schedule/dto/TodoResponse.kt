package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unicorn.server.domain.schedule.port.dto.TodoResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "할일 응답")
data class TodoResponse(
	@field:Schema(description = "할일 ID", example = "TD20240801090000001")
	val id: String,

	@field:Schema(description = "할일 제목", example = "숙소 예약하기")
	val title: String,

	@field:Schema(description = "담당자 및 각 담당자의 체크 상태 목록")
	val members: List<TodoMemberResponse>,

	@field:Schema(description = "로그인한 멤버가 이 할일의 담당자인지 여부", example = "true")
	@get:JsonProperty("isMy")
	val isMy: Boolean,
) {
	companion object {
		fun from(result: TodoResult): TodoResponse = TodoResponse(
			id = result.todoId.value,
			title = result.title,
			members = result.members.map(TodoMemberResponse::from),
			isMy = result.isMy,
		)
	}
}
