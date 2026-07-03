package com.unicorn.server.domain.circle.exception

import com.unicorn.server.common.exception.BusinessException

class CircleNotFoundException(circleId: String) :
	BusinessException(CircleErrorCode.CIRCLE_NOT_FOUND, "circleId=$circleId")
