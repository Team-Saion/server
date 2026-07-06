package com.unicorn.server.domain.circle.port.out

import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId

interface CircleOutPort {
	fun save(circle: Circle): Circle
	fun findById(circleId: CircleId): Circle?
	fun findAllByOwnerId(ownerId: MemberId): List<Circle>
}
