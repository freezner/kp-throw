package com.freezner.service.kpthrow.helper

import com.freezner.service.kpthrow.service.InMoneyService
import org.springframework.stereotype.Component

@Component
class HeaderValidator {
    fun valid(
        userId: Long,
        roomId: String
    ): Boolean {
        InMoneyService.logger.info(">>> userId : $userId")
        InMoneyService.logger.info(">>> roomId : $roomId")

        return !(userId == 0L ||roomId == "")
    }
}