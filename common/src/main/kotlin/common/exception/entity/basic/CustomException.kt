package common.exception.entity.basic

import common.exception.ExceptionTools
import java.lang.Exception

abstract class CustomException(override var message : String? = null) : Exception(message) {

    fun stackTraceToString() : String {
        return ExceptionTools.stackTraceToString(this)
    }
}