package com.krypton.cloud.service.handler.http

import org.springframework.http.HttpStatus

interface ErrorHandler {

    fun httpError(message : String) : HttpStatus
}