package com.krypton.cloud.service.util.commons

import java.io.PrintWriter
import java.io.StringWriter

object ExceptionTools {

    fun stackTraceToString(exception: Exception) : String {
        val sw = StringWriter()
        exception.printStackTrace(PrintWriter(sw))
        return sw.buffer.toString()
    }
}