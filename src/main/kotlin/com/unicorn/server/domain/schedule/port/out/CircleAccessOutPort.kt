package com.unicorn.server.domain.schedule.port.out

interface CircleAccessOutPort {
	fun existsById(circleId: Long): Boolean

	fun isMember(circleId: Long, memberId: String): Boolean

	fun isInitiator(circleId: Long, memberId: String): Boolean
}
