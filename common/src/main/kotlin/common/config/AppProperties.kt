package common.config

import common.tools.CommonTools
import java.io.File

object AppProperties {

    // folder used for cloud storage
    val root = when (CommonTools.runsInsideContainer()) {
        true -> "/Cloud"
        false -> "${System.getProperty("user.home")}/Cloud"
    }

    val storageFolder   = File("$root/Storage")
    val trashFolder     = File("$root/Trash")
    val logsFolder      = File("$root/Logs")

}
