package com.freezner.service.kpthrow.domain

import java.time.LocalDateTime

data class ResponseApi (
    var success: Boolean,
    var errorCode: String,
    var message: String?,
    var data : List<Any?>? = null
)

data class ThrownMoneyDetail (
    val thrownDate: LocalDateTime,
    val thrownAmount: Int,
    val takenAmount: Int,
    val takenList: List<Any>?
)

data class TakenList (
    var userId: Long,
    var amount: Int
)