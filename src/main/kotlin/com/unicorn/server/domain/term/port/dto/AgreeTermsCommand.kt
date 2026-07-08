package com.unicorn.server.domain.term.port.dto

data class AgreeTermsCommand(
	val memberId: String,
	val termIds: List<Long>,
)
