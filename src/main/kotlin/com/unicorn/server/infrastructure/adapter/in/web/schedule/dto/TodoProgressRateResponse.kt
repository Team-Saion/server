package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.port.dto.TodoResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "할 일 진행률")
data class TodoProgressRateResponse(
	@field:Schema(description = "전체 할일 진행률 %. 담당자 전원이 체크한 할일을 완료로 계산합니다.", example = "40")
	val rate: Int,

	@field:Schema(description = "전체 완료 할일 수", example = "2")
	val doneCount: Int,

	@field:Schema(description = "전체 할일 수", example = "5")
	val totalCount: Int,

	@field:Schema(description = "내가 담당자인 할일 진행률 %. 담당자 전원이 체크한 할일을 완료로 계산합니다.", example = "50")
	val myRate: Int,

	@field:Schema(description = "내가 담당자인 완료 할일 수", example = "1")
	val myDoneCount: Int,

	@field:Schema(description = "내가 담당자인 전체 할일 수", example = "2")
	val myTotalCount: Int,
) {
	companion object {
		fun from(todos: List<TodoResult>): TodoProgressRateResponse {
			val myTodos = todos.filter { it.isMy }
			val doneCount = todos.count { it.isCompleted }
			val myDoneCount = myTodos.count { it.isCompleted }

			return TodoProgressRateResponse(
				rate = calculateRate(doneCount, todos.size),
				doneCount = doneCount,
				totalCount = todos.size,
				myRate = calculateRate(myDoneCount, myTodos.size),
				myDoneCount = myDoneCount,
				myTotalCount = myTodos.size,
			)
		}

		private fun calculateRate(doneCount: Int, totalCount: Int): Int =
			if (totalCount == 0) 0 else doneCount * 100 / totalCount
	}
}
