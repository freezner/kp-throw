package com.freezner.service.kpthrow.helper

import com.freezner.service.kpthrow.domain.ResponseApi
import javassist.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import java.lang.RuntimeException

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun BadRequestException (e: RuntimeException): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5001",
        message = e.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun MethodArgumentNotValidException (e: MethodArgumentNotValidException): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5002",
        message = e.message
    )

    @ExceptionHandler(MissingRequestHeaderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun MissingRequestHeaderException (e: MissingRequestHeaderException): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5003",
        message = e.message
    )

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun NotFoundException (e: NotFoundException): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5004",
        message = e.message
    )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun InternalServerErrorException (e: Exception): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5006",
        message = e.message
    )

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun NoHandlerFoundException (e: NoHandlerFoundException): ResponseApi = ResponseApi(
        success = false,
        errorCode = "5007",
        message = e.message
    )
}