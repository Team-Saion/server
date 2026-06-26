package com.unicorn.server.domain.term.port.`in`

import com.unicorn.server.domain.term.port.dto.AgreeTermsCommand

interface AgreeTermsInPort {
	fun agreeTerms(command: AgreeTermsCommand)
}
