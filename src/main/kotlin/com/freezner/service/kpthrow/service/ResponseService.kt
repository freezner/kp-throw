package com.freezner.service.kpthrow.service

import com.freezner.service.kpthrow.domain.ResponseApi
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ResponseService {
    fun success (
        responseMessage: String?,
        responseData: List<Any>?
    ): ResponseApi = ResponseApi(
        success = true,
        message = responseMessage,
        data = responseData
    )

    fun fail (
        responseMessage: String?
    ): ResponseApi = ResponseApi(
        success = false,
        message = responseMessage
    )
}