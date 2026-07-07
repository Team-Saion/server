package com.unicorn.server.domain.schedule.enums

enum class ConfirmationType(
	val label: String,
	val available: Boolean
) {

	CONFIRMED("확인했어요", true),
	ETC("기타", true),

}
