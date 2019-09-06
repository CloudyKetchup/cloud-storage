package com.krypton.cloud.exception.io

import com.krypton.cloud.exception.entity.basic.CustomException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NotFoundException : CustomException()
