package com.unicorn.server.domain.schedule

data class TodoMember(
	val memberId: String,
	val checked: Boolean = false,
) {
	fun check(): TodoMember = copy(checked = true)

	fun uncheck(): TodoMember = copy(checked = false)
}
