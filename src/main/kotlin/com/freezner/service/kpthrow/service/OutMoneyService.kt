package com.freezner.service.kpthrow.service

import com.freezner.service.kpthrow.domain.ResponseApi
import com.freezner.service.kpthrow.lib.*
import com.freezner.service.kpthrow.repository.OutMoneyRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OutMoneyService (
    private val responseService: ResponseService,
    private val outMoneyRepository: OutMoneyRepository,
    private val headerValidator: HeaderValidator
)
{
    companion object: KLogging() {
        private const val EXPIRE_MINUTE     = 10L // 조회 만료 시간(분)

        const val TAKE_ONE_TIME             = "뿌리기 당 한 사용자는 한번만 받을 수 있습니다."
        const val INVALID_TOKEN             = "존재하지 않는 토큰입니다."
        const val DONT_TAKE_MONEY_MYSELF    = "자신이 뿌리기한 건은 자신이 받을 수 없습니다."
        const val SAME_ROOM_MEMBER_ONLY     = "뿌린 자가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다."
        const val EXPIRED_TIME              = "뿌린 머니의 조회는 ${EXPIRE_MINUTE}분간만 유효합니다."
        const val TAKE_OK                   = "받기 성공!!"
        const val NOT_ENOUGH_MONEY          = "받을 수 있는 머니가 모두 소진되었습니다."
    }

    /**
     * 머니 받기
     *
     * @param token 뿌린 머니의 토큰
     * @param userId X-USER-ID Header
     * @param roomId X-ROOM-ID Header
     * @return ResponseApi
     */
    fun takeMoney(token: String, userId: Long, roomId: String): ResponseApi = try {

        // 헤더 검증
        if (!headerValidator.valid(userId, roomId))
            throw Exception(InMoneyService.NOT_FOUND_USER_OR_ROOM_ID)

        val entity = outMoneyRepository.findAllByToken(token)

        if (entity.isEmpty()) throw Exception(INVALID_TOKEN)

        entity.forEach {
            // 뿌리기 당 한 사용자는 한번만 받을 수 있습니다.
            if (it.outUserId == userId)
                throw Exception(TAKE_ONE_TIME)

            // 자신이 뿌리기한 건은 자신이 받을 수 없습니다.
            if (it.inUserId == userId)
                throw Exception(DONT_TAKE_MONEY_MYSELF)

            if (it.outUserId == null) {
                // 뿌린 자가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.
                if (it.roomId != roomId)
                    throw Exception(SAME_ROOM_MEMBER_ONLY)

                // 뿌린 건은 10분간만 유효합니다. 뿌린지 10분이 지난 요청에 대해서는 받지 실패 응답이 내려가야 합니다.
                if (LocalDateTime.now() > it.createAt.plusMinutes(EXPIRE_MINUTE))
                    throw Exception("$EXPIRED_TIME ${it.createAt.plusMinutes(EXPIRE_MINUTE)} / ${LocalDateTime.now()}")

                // outUserId 업데이트
                outMoneyRepository.save(it.apply {
                    outUserId = userId
                    updateAt = LocalDateTime.now()
                })

                // 받기 완료되면 루프를 빠져나온다.
                return responseService.success(TAKE_OK, null)
            }
        }

        responseService.success(NOT_ENOUGH_MONEY, null)
    } catch (e: Exception) {
        responseService.fail(e.message)
    }
}