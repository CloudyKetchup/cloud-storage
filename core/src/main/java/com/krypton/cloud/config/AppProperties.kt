package com.krypton.cloud.config

import com.krypton.cloud.service.util.common.CommonTools
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.io.File

@Service
@EnableConfigurationProperties
class AppProperties {

    // folder used for cloud storage
    val root = when (CommonTools.runsInsideContainer()) {
        true -> File("/Cloud")
        false -> File("${System.getProperty("user.home")}/Cloud")
    }
}
