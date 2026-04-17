package com.unicorn.server.domain.user.exception

import com.unicorn.server.common.exception.BusinessException

class UserNotFoundException(userId: String) : BusinessException(UserErrorCode.USER_NOT_FOUND, "userId=$userId")
