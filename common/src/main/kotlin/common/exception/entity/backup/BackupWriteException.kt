package common.exception.entity.backup

import common.exception.entity.basic.CustomException

class BackupWriteException(override var message : String? = null): CustomException(message)
