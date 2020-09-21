package com.freezner.service.kpthrow.helper

import mu.KLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Component
class TokenManager {
    companion object: KLogging() { }

    fun generateToken (length: Int = 3): String {
        var token = ""

        for (i in 0..length.minus(1)) {
            token = token.plus(generateRandomString((0..1).random()))
        }

        return token
    }

    private fun generateRandomString(digest: Int = 0):String {
        val output = ('a'..'z').random().toString()

        if (digest.rem(2) == 0) output.toUpperCase()

        return output
    }
}
