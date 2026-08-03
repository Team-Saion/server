package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.vo.TodoId

interface TodoIdGenerator {
	fun next(): TodoId
}
