package com.unicorn.server.domain.circle.port.out

import com.unicorn.server.domain.circle.vo.CircleId

interface CircleIdGenerator {
	fun next(): CircleId
}
