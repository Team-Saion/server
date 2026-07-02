package com.unicorn.server.domain.home.port.`in`

import com.unicorn.server.domain.home.port.dto.HomeMemberDto
import com.unicorn.server.domain.home.port.dto.HomeView

interface HomeQueryInPort {
	fun getHome(circleId: String, requesterId: String): HomeView
	fun getMembers(circleId: String, requesterId: String): List<HomeMemberDto>
}
