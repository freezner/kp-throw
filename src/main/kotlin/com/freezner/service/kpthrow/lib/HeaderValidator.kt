package com.freezner.service.kpthrow.lib

import com.freezner.service.kpthrow.service.InMoneyService
import org.springframework.stereotype.Service

@Service
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