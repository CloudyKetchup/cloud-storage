package common.exception.entity.io

import common.exception.entity.basic.CustomException

class FolderIOException(override var message : String? = null) : CustomException(message)