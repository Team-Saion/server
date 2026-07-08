package com.unicorn.server.domain.schedule.port.out

interface CircleAccessOutPort {
	fun existsById(circleId: String): Boolean

	fun isMember(circleId: String, memberId: String): Boolean

	fun isInitiator(circleId: String, memberId: String): Boolean
}
