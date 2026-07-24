package com.unicorn.server.common.port.out.alert

interface ErrorAlertPort {
    fun sendErrorAlert(e: Exception)
}
