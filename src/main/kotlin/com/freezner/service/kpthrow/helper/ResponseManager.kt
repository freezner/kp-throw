package com.freezner.service.kpthrow.helper

import com.freezner.service.kpthrow.domain.ResponseApi
import org.springframework.stereotype.Component

@Component
class ResponseManager {
    fun success (
        responseMessage: String?,
        responseData: List<Any>?
    ): ResponseApi = ResponseApi(
        success = true,
        errorCode = "0000",
        message = responseMessage,
        data = responseData
    )

    fun fail (
        responseMessage: String?
    ): ResponseApi {
        val errorMessageDivide = responseMessage?.split("@")
        val errorCode: String
        val errorMessage: String

        if (errorMessageDivide?.size!! > 0) {
            errorCode = errorMessageDivide[0]
            errorMessage = errorMessageDivide[1]
        } else {
            errorCode = ""
            errorMessage = errorMessageDivide[0]
        }

        return ResponseApi(
            success = false,
            errorCode = errorCode,
            message = errorMessage
        )
    }
}