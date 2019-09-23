package common.exception.log

import common.exception.entity.basic.CustomException

class LogException(override var message : String? = null) : CustomException(message)