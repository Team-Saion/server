package com.unicorn.server.common.port.out.alert

interface AlertOutPort {
    fun sendErrorAlert(e: Exception)
}
