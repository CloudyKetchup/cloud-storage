package util.file

import common.model.FileType
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.DecimalFormat
import java.util.EnumSet.allOf

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
        return allOf(FileType::class.java)
                .stream()
                .filter { e : FileType -> e.type.toLowerCase() == FilenameUtils.getExtension(file.name) }
                .findAny()
                .orElse(FileType.OTHER)
    }
}