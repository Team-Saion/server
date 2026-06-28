package com.unicorn.server.infrastructure.adapter.`in`.web.term.dto

import jakarta.validation.constraints.NotEmpty

data class AgreeTermsRequest(
	@field:NotEmpty val termIds: List<Long>,
)
