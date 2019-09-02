package com.krypton.cloud.exception.entity.database

import com.krypton.cloud.exception.entity.basic.CustomException

class FolderPersistenceException(override var message : String? = null) : CustomException(message)