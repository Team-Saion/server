package com.unicorn.server.domain.term.vo

@JvmInline
value class MemberTermId(val value: Long) {
	override fun toString(): String = value.toString()

	companion object {
		fun of(value: Long): MemberTermId = MemberTermId(value)
	}
}
