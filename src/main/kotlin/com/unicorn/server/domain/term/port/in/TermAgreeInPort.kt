package com.unicorn.server.domain.term.port.`in`

import com.unicorn.server.domain.term.port.dto.AgreeTermsCommand

interface TermAgreeInPort {
	fun agreeTerms(command: AgreeTermsCommand)
}
