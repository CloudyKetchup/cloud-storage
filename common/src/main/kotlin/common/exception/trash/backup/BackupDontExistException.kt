package common.exception.trash.backup

import common.exception.entity.basic.CustomException

class BackupDontExistException(override var message : String? = null) : CustomException(message)