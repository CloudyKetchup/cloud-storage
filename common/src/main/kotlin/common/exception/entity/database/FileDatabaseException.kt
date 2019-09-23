package common.exception.entity.database

import common.exception.entity.basic.CustomException

class FileDatabaseException(override var message : String? = null) : CustomException(message)