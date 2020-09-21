package com.freezner.service.kpthrow.domain

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class RequestInMoney (
    var userId: Long = 0L,

    var roomId: String = "",

    @NotNull(message = "뿌릴 금액이 없습니다.")
    @Min(value = 1, message = "뿌릴 금액이 올바르지 않습니다.")
    var amount: Int = 0,

    @NotNull(message = "뿌릴 금액을 보낼 최대 인원 수가 없습니다.")
    @Min(value = 1, message = "최소 1명 이상 뿌릴 수 있습니다.")
    var limit: Int = 0
)