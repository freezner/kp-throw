package com.freezner.service.kpthrow.service

import com.freezner.service.kpthrow.domain.ResponseApi
import com.freezner.service.kpthrow.repository.OutMoneyRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OutMoneyService (
    private val responseService: ResponseService,
    private val outMoneyRepository: OutMoneyRepository
)
{
    companion object: KLogging() {

    }

    fun takeMoney(token: String, userId: Long, roomId: String): ResponseApi = try {
        val entity = outMoneyRepository.findAllByToken(token)

        if (entity.isEmpty()) throw Exception("존재하지 않는 토큰입니다.")

        entity.forEach {
            // @ALREADY_TAKE_MONEY_EXCEPTION
            // 뿌리기 당 한 사용자는 한번만 받을 수 있습니다.
            if (it.outUserId == userId)
                throw Exception("뿌리기 당 한 사용자는 한번만 받을 수 있습니다.")

            // @MYSELF_TAKE_MONEY_EXCEPTION
            // 자신이 뿌리기한 건은 자신이 받을 수 없습니다.
            if (it.inUserId == userId)
                throw Exception("자신이 뿌리기한 건은 자신이 받을 수 없습니다.")

            if (it.outUserId == null) {
                // @ROOM_MISMATCH_EXCEPTION
                // 뿌린 자가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.
                if (it.roomId != roomId)
                    throw Exception("뿌린 자가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.")

                // @EXPIRED_TOKEN_EXCEPTION
                // 뿌린 건은 10분간만 유효합니다. 뿌린지 10분이 지난 요청에 대해서는 받지 실패 응답이 내려가야 합니다.
                if (it.createAt.plusMinutes(10) > LocalDateTime.now())
                // throw Exception("뿌린 건은 10분간만 유효합니다.")

                // outUserId 업데이트
                outMoneyRepository.save(it.apply {
                    outUserId = userId
                    updateAt = LocalDateTime.now()
                })

                // 받기 완료되면 루프를 빠져나온다.
                return responseService.success("받기 완료!", null)
            }
        }

        responseService.success("아무일 없음..", null)
    } catch (e: Exception) {
        responseService.fail(e.message)
    }
}