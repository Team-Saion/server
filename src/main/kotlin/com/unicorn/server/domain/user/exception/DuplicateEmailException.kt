package com.unicorn.server.domain.user.exception

import com.unicorn.server.common.exception.BusinessException

class DuplicateEmailException(email: String) : BusinessException(UserErrorCode.DUPLICATE_EMAIL, "email=$email")
