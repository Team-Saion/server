package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "할일 목록 조회 응답")
data class TodoListResponse(
	@field:Schema(description = "로그인한 멤버가 담당자인 할일 목록입니다. 할일 생성일 기준 최신순으로 정렬됩니다.")
	val myTodos: List<TodoResponse>,

	@field:Schema(description = "일정에 연결된 전체 할일 목록입니다. 할일 생성일 기준 최신순으로 정렬됩니다.")
	val allTodos: List<TodoResponse>,
)
