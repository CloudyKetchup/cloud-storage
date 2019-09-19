package com.krypton.cloud.service.util.file

import com.krypton.cloud.model.FileType
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.DecimalFormat
import java.util.*

object FileTools {

    fun getFileSize(size: Long): String {
        val df = DecimalFormat("0.00")

        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb

        return when {
            size < sizeMb -> df.format((size / sizeKb).toDouble()) + " Kb"
            size < sizeGb -> df.format((size / sizeMb).toDouble()) + " Mb"
            size < sizeGb * sizeKb -> df.format((size / sizeGb).toDouble()) + " Gb"
            else -> ""
        }
    }

    fun getFileExtension(file: File): FileType {
        return EnumSet.allOf(FileType::class.java)
                .stream()
                .filter { e -> e.type.toLowerCase() == FilenameUtils.getExtension(file.name) }
                .findAny()
                .orElse(FileType.OTHER)
    }
}