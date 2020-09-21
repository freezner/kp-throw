package com.freezner.service.kpthrow.controller.v1

import com.freezner.service.kpthrow.domain.RequestInMoney
import com.freezner.service.kpthrow.domain.ResponseApi
import com.freezner.service.kpthrow.service.InMoneyService
import com.freezner.service.kpthrow.service.OutMoneyService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.*

@RestController
@RequestMapping("/api/v1")
class ApiController (
    private val inMoneyService: InMoneyService,
    private val outMoneyService: OutMoneyService
)
{
    // 뿌리기
    @PostMapping("/splash")
    fun throwMoney(
        @RequestHeader(value = "X-USER-ID", required = true) userId: Long,
        @RequestHeader(value = "X-ROOM-ID", required = true) roomId: String,
        @RequestBody @Validated requestInMoneyDto: RequestInMoney
    ): ResponseApi = inMoneyService.setInMoney(requestInMoneyDto, userId, roomId)

    // 받기
    @PutMapping("/take/{token:[0-9a-zA-z]{3}}")
    fun takeMoney(
        @RequestHeader(value = "X-USER-ID", required = true) userId: Long,
        @RequestHeader(value = "X-ROOM-ID", required = true) roomId: String,
        @PathVariable(value = "token") @NotBlank token: String
    ): ResponseApi = outMoneyService.takeMoney(token, userId, roomId)

    // 조회
    @GetMapping("/info/{token:[0-9a-zA-z]{3}}")
    fun throwMoneyInfo(
        @RequestHeader(value = "X-USER-ID", required = true) userId: Long,
        @RequestHeader(value = "X-ROOM-ID", required = true) roomId: String,
        @PathVariable(value = "token") token: String
    ): ResponseApi = inMoneyService.showMeTheMoney(token, userId, roomId)
}