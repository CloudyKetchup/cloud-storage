package com.krypton.cloud.exception.entity.basic

import com.krypton.cloud.service.util.commons.ExceptionTools
import java.lang.Exception

abstract class CustomException(override var message : String? = null) : Exception(message) {

    fun stackTraceToString() : String {
        return ExceptionTools.stackTraceToString(this)
    }
}