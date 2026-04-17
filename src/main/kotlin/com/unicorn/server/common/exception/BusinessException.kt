package com.unicorn.server.common.exception

open class BusinessException(
	val errorCode: ErrorCode,
	detail: String? = null,
	cause: Throwable? = null,
) : RuntimeException(

	if (detail == null) errorCode.message else "${errorCode.message} - $detail",
	cause,

)
