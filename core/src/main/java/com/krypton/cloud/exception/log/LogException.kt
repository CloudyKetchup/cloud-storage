package com.krypton.cloud.exception.log

import com.krypton.cloud.exception.entity.basic.CustomException

class LogException(override var message : String? = null) : CustomException(message)