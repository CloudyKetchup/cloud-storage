package com.krypton.cloud.service.util.common

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Tools that are not related to something specific, but used in many other services
 * */
object CommonTools {

    /**
     * Check if app runs inside docker deployment
     *
     * @return boolean depending on if runs in docker container or not
     * */
    fun runsInsideContainer() : Boolean {
        val path = Paths.get("/proc/1/cgroup")

        if (path.toFile().exists()) {
            return try {
                Files.lines(path)
                        .use { stream -> stream.anyMatch { it.contains("/docker") } }
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
        return false
    }
}