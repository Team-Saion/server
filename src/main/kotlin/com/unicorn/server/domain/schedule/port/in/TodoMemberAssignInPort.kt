package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.AssignTodoMembersCommand

interface TodoMemberAssignInPort {
	fun assignMembers(command: AssignTodoMembersCommand)
}
