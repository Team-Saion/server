package com.unicorn.server.infrastructure.adapter.out.stub.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort

@PersistenceAdapter
class StubCircleAccessAdapter : CircleAccessOutPort {

	override fun existsById(circleId: Long): Boolean =
		throw UnsupportedOperationException("Circle access is not yet implemented")

	override fun isMember(circleId: Long, memberId: String): Boolean =
		throw UnsupportedOperationException("Circle access is not yet implemented")

	override fun isInitiator(circleId: Long, memberId: String): Boolean =
		throw UnsupportedOperationException("Circle access is not yet implemented")
}
