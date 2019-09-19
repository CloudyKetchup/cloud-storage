package com.krypton.cloud.service.util.exception

import java.io.PrintWriter
import java.io.StringWriter

object ExceptionTools {

    fun stackTraceToString(exception: Exception) : String {
        val sw = StringWriter()
        exception.printStackTrace(PrintWriter(sw))
        return sw.buffer.toString()
    }
}