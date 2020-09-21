package com.freezner.service.kpthrow.lib

import mu.KLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TokenManager {
    companion object: KLogging() { }

    fun generateToken (
        sourceA: Any,
        sourceB: Any?,
        length: Int = 3
    ): String {
        val nowDatetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"))
        val hashString = Base64.getEncoder().encode("${sourceA}${sourceB}${nowDatetime}".toByteArray()).toString()

        logger.info(">>> HashSource : ${hashString}(${hashString.length.minus(length)})")

        val token = hashString.slice(
            hashString.length.minus(length)..hashString.length.minus(1)
        )

        logger.info(">>> get token : $token")

        return token
    }
}