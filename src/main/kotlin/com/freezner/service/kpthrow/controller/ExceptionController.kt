package com.freezner.service.kpthrow.controller

import com.freezner.service.kpthrow.domain.ResponseApi
import javassist.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.RuntimeException

@RestControllerAdvice
class ExceptionController {
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun BadRequestException (e: RuntimeException): ResponseApi = ResponseApi(
        success = false,
        message = e.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun MethodArgumentNotValidException (e: MethodArgumentNotValidException): ResponseApi = ResponseApi(
        success = false,
        message = e.message
    )

    @ExceptionHandler(MissingRequestHeaderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun MissingRequestHeaderException (e: MissingRequestHeaderException): ResponseApi = ResponseApi(
        success = false,
        message = e.message
    )

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun NotFoundException (e: NotFoundException): ResponseApi = ResponseApi(
        success = false,
        message = e.message
    )
}