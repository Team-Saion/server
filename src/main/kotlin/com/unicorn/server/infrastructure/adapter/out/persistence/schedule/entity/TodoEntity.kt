package com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity

import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.TodoMember
import com.unicorn.server.domain.schedule.vo.TodoId
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "todo")
class TodoEntity protected constructor() {
	@Id
	@Column(name = "todo_id", nullable = false, length = 19)
	var id: String = ""
		protected set

	@Column(name = "schedule_id", nullable = false, length = 19)
	var scheduleId: String = ""
		protected set

	@Column(name = "circle_id", nullable = false, length = 21)
	var circleId: String = ""
		protected set

	@Column(name = "title", nullable = false, length = 30)
	var title: String = ""
		protected set

	@Column(name = "created_by", nullable = false, length = 100)
	var createdBy: String = ""
		protected set

	@Column(name = "del_yn", nullable = false, length = 1)
	var delYn: String = "N"
		protected set

	@ElementCollection
	@CollectionTable(name = "todo_member", joinColumns = [JoinColumn(name = "todo_id")])
	var members: MutableList<TodoMemberEmbeddable> = mutableListOf()
		protected set

	constructor(todo: Todo) : this() {
		id = todo.id.value
		applyDomain(todo)
	}

	fun update(todo: Todo) = applyDomain(todo)

	fun delete() {
		delYn = "Y"
	}

	fun toDomain(): Todo = Todo.reconstitute(
		id = TodoId.of(id),
		scheduleId = scheduleId,
		circleId = circleId,
		title = title,
		createdBy = createdBy,
		members = members.map { TodoMember(it.memberId, it.checked) },
	)

	private fun applyDomain(todo: Todo) {
		scheduleId = todo.scheduleId
		circleId = todo.circleId
		title = todo.title
		createdBy = todo.createdBy
		members = todo.members
			.map { TodoMemberEmbeddable(it.memberId, it.checked) }
			.toMutableList()
	}
}
