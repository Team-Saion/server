package com.unicorn.server.infrastructure.adapter.`in`.web.term.dto

import com.unicorn.server.domain.term.Term
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/** GET /v1/terms 응답으로 노출되는 약관 1건의 DTO다. */
@Schema(description = "약관 응답")
data class TermResponse(
	@field:Schema(description = "약관 버전 ID")
	val id: String,

	@field:Schema(description = "약관 유형 코드")
	val termCode: String,

	@field:Schema(description = "약관 제목")
	val title: String,

	@field:Schema(description = "약관 본문 URL", nullable = true)
	val contentUrl: String?,

	@field:Schema(description = "약관 버전")
	val version: Int,

	@field:Schema(description = "필수 동의 여부")
	val required: Boolean,

	@field:Schema(description = "발효 시각")
	val effectiveAt: LocalDateTime,
) {
	companion object {
		fun from(term: Term): TermResponse = TermResponse(
			id = term.id.toString(),
			termCode = term.termCode.name,
			title = term.title,
			contentUrl = term.contentUrl,
			version = term.version,
			required = term.required,
			effectiveAt = term.effectiveAt,
		)
	}
}
