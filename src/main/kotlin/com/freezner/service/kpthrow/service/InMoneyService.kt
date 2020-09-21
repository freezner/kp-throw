package com.freezner.service.kpthrow.service

import com.freezner.service.kpthrow.domain.RequestInMoney
import com.freezner.service.kpthrow.domain.ResponseApi
import com.freezner.service.kpthrow.domain.TakenList
import com.freezner.service.kpthrow.domain.ThrownMoneyDetail
import com.freezner.service.kpthrow.lib.TokenManager
import com.freezner.service.kpthrow.repository.entity.InMoney
import com.freezner.service.kpthrow.repository.InMoneyRepository
import com.freezner.service.kpthrow.repository.OutMoneyRepository
import com.freezner.service.kpthrow.repository.entity.OutMoney
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class InMoneyService (
    private val inMoneyRepository: InMoneyRepository,
    private val outMoneyRepository: OutMoneyRepository,
    private val responseService: ResponseService
)
{
    companion object: KLogging() {
        const val NOT_FOUND_USER_ID     = "userId가 존재하지 않습니다."
        const val NOT_FOUND_ROOM_ID     = "roomId가 존재하지 않습니다."
        const val FAILED_GENERATE_TOKEN = "토큰 생성 실패"
        const val IN_MONEY_OK           = "뿌리기 성공!"
        const val IN_MONEY_FAIL         = "뿌리기 실패"
        const val SHOW_MONEY_FAIL       = "조회 실패"
    }

    @Transactional
    fun setInMoney(requestDto: RequestInMoney, userId: Long, roomId: String): ResponseApi = try {
        logger.info(">>> userId : {}", requestDto.userId)
        logger.info(">>> roomId : {}", requestDto.roomId)

        if (userId == 0L) throw Exception(NOT_FOUND_USER_ID)
        if (roomId == "") throw Exception(NOT_FOUND_ROOM_ID)

        val makeToken = TokenManager().generateToken(userId, roomId).let {
            if (it == "") throw Exception(FAILED_GENERATE_TOKEN) else it
        }

        val entity = InMoney().apply {
            this.userId = userId
            this.roomId = roomId
            token       = makeToken
            amount      = requestDto.amount
            limit       = requestDto.limit
        }

        inMoneyRepository.save(entity)

        initOutMoney(entity)

        responseService.success(IN_MONEY_OK, listOf(entity))
    } catch (e: Exception) {
        responseService.fail("$IN_MONEY_FAIL ${e.message}")
    }

    fun showMeTheMoney(token: String, userId: Long, roomId: String): ResponseApi = try {
        val entity= inMoneyRepository.findByToken(token)

        // 뿌린 사람 자신만 조회할 수 있습니다.
        if (entity.userId != userId)
            throw Exception("뿌린 사람 자신만 조회할 수 있습니다.")

        // 뿌린 건에 대한 조회는 7일 동안 할 수 있습니다.
        // if (entity.createAt.plusDays(7) > LocalDateTime.now())
            // throw Exception("조회 가능 기간이 지났습니다. 조회는 뿌린 후 7일간 가능합니다.")

        // 뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보 ([받은 금액, 받은 사용자 아이디] 리스트
        val entityOutMoney = outMoneyRepository.findAllByTokenAndOutUserIdIsNotNull(token)

        val thrownMoneyDetail = listOf(
            ThrownMoneyDetail(
                thrownDate      = entity.createAt,
                thrownAmount    = entityOutMoney.sumBy { it.amount },
                takenAmount     = entity.amount,
                takenList       = takenListBuilder(entityOutMoney)
            )
        )

        logger.info(">>> thrownMoneyDetail :  $thrownMoneyDetail")

        responseService.success("조회 성공", thrownMoneyDetail)
    } catch (e: Exception) {
        responseService.fail("$SHOW_MONEY_FAIL ${e.message}")
    }

    private fun takenListBuilder(originList: List<OutMoney>): List<TakenList> {
        val resultList = mutableListOf<TakenList>()

        for(i in 0..originList.size.minus(1)) {
            resultList.add(
                TakenList(
                    userId = originList[i].outUserId!!,
                    amount = originList[i].amount
                )
            )
        }

        return resultList.toList()
    }

    private fun initOutMoney(inMoney: InMoney) {
        val amountModCheck = inMoney.amount.rem(inMoney.limit)
        val dividedAmount = inMoney.amount.div(inMoney.limit);
        val loopCount = inMoney.limit.minus(1)

        logger.info(">>> $inMoney.amount / $inMoney.limit => Divided amount : $amountModCheck")

        for (i in 0..loopCount) {
            val outMoneyEntity = OutMoney().apply {
                token = inMoney.token
                roomId = inMoney.roomId.toString()
                inUserId = inMoney.userId

                // 뿌리기 금액 배분 시 정수값으로 나눠 떨어지지 않을 경우를 처리한다.
                amount = if (amountModCheck > 0 && loopCount == i) {
                    inMoney.amount.minus(dividedAmount.times(loopCount))
                } else {
                    dividedAmount
                }

                logger.info(">>> setAmount[$i] : $amount")
            }

            logger.info(">>> Make entity[$i] : $outMoneyEntity")

            outMoneyRepository.save(outMoneyEntity)
        }
    }
}