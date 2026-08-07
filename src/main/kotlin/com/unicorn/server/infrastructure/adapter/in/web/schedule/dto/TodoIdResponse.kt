package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.vo.TodoId
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "할일 생성 응답")
data class TodoIdResponse(
	@field:Schema(description = "생성된 할일 ID", example = "TD20240801090000001")
	val todoId: String,
) {
	companion object {
		fun of(todoId: TodoId) = TodoIdResponse(todoId.value)
	}
}
