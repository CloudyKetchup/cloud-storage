package com.krypton.cloud.exception.entity.io

import com.krypton.cloud.exception.entity.basic.CustomException

class FileIOException(override var message : String? = null) : CustomException(message)