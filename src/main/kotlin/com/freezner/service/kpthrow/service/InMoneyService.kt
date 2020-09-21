package com.freezner.service.kpthrow.service

import com.freezner.service.kpthrow.domain.RequestInMoney
import com.freezner.service.kpthrow.domain.ResponseApi
import com.freezner.service.kpthrow.domain.TakenList
import com.freezner.service.kpthrow.domain.ThrownMoneyDetail
import com.freezner.service.kpthrow.lib.HeaderValidator
import com.freezner.service.kpthrow.lib.TokenManager
import com.freezner.service.kpthrow.repository.entity.InMoney
import com.freezner.service.kpthrow.repository.InMoneyRepository
import com.freezner.service.kpthrow.repository.OutMoneyRepository
import com.freezner.service.kpthrow.repository.entity.OutMoney
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 머니 뿌리기 기능에 관련한 서비스
 *
 * 머니를 뿌리기 초기화 및 조회하는 기능을 포함.
 *
 * @property InMoneyRepository in_money 엔티티에 대응하는 JPA 레포지토리
 * @property OutMoneyRepository out_money 엔티티에 대응하는 JPA 레포지토리
 * @property ResponseService 호출 결과 응답 클래스
 */
@Service
class InMoneyService (
    private val inMoneyRepository: InMoneyRepository,
    private val outMoneyRepository: OutMoneyRepository,
    private val responseService: ResponseService,
    private val headerValidator: HeaderValidator

)
{
    companion object: KLogging() {
        private const val EXPIRE_DAY            = 7L

        const val NOT_FOUND_USER_OR_ROOM_ID     = "userId나 roomId가 존재하지 않습니다."
        const val FAILED_GENERATE_TOKEN         = "토큰 생성 실패"
        const val IN_MONEY_OK                   = "뿌리기 성공!!"
        const val IN_MONEY_FAIL                 = "뿌리기 실패"
        const val SHOW_MONEY_OK                 = "조회 성공!!"
        const val SHOW_MONEY_FAIL               = "조회 실패"
        const val EXPIRED_THROW_MONEY           = "조회 가능 기간이 만료됐습니다. 조회는 뿌리기 후 ${EXPIRE_DAY}일간 가능합니다."
        const val VIEW_ONLY_MYSELF              = "뿌린 사람 자신만 조회할 수 있습니다."
    }

    /**
     * 머니 뿌리기
     *
     * @param requestDto 머니 뿌리기 RequestDto
     * @param userId X-USER-ID Header
     * @param roomId X-ROOM-ID Header
     * @return ResponseApi
     */
    @Transactional
    fun setInMoney(requestDto: RequestInMoney, userId: Long, roomId: String): ResponseApi = try {

        // 헤더 검증
        if (!headerValidator.valid(userId, roomId))
            throw Exception(NOT_FOUND_USER_OR_ROOM_ID)

        // 토큰을 생성한다.
        val makeToken = TokenManager().generateToken(userId, roomId).let {
            if (it == "") throw Exception(FAILED_GENERATE_TOKEN) else it
        }

        // 머니 뿌리기 엔티티에 요청 결과를 저장한다.
        val entity = InMoney().apply {
            this.userId = userId
            this.roomId = roomId
            this.token  = makeToken
            this.amount = requestDto.amount
            this.limit  = requestDto.limit
        }

        inMoneyRepository.save(entity)

        // 머니 받기 엔티티에 뿌린 금액을 배분한다.
        initOutMoney(entity)

        responseService.success(IN_MONEY_OK, listOf(entity))
    } catch (e: Exception) {
        responseService.fail("$IN_MONEY_FAIL ${e.message}")
    }

    /**
     * 머니 뿌리기 조회
     *
     * @param token 머니 뿌리기 시 발급된 토큰
     * @param userId X-USER-ID Header
     * @param roomId X-ROOM-ID Header
     * @return ResponseApi
     */
    fun showMeTheMoney(token: String, userId: Long, roomId: String): ResponseApi = try {

        // 헤더 검증
        if (!headerValidator.valid(userId, roomId))
            throw Exception(NOT_FOUND_USER_OR_ROOM_ID)

        // 토큰에 해당하는 머니 뿌리기 정보를 조회한다.
        val entity= inMoneyRepository.findByToken(token)

        // 뿌린 사람 자신만 조회할 수 있습니다.
        if (entity.userId != userId)
            throw Exception(VIEW_ONLY_MYSELF)

        // 뿌린 건에 대한 조회는 7일 동안 할 수 있습니다.
        if (LocalDateTime.now() > entity.createAt.plusDays(EXPIRE_DAY))
            throw Exception(EXPIRED_THROW_MONEY)

        // 뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보 ([받은 금액, 받은 사용자 아이디] 리스트
        val entityOutMoney = outMoneyRepository.findAllByTokenAndOutUserIdIsNotNull(token)

        // 조회 정보를 리스트로 만든다.
        val thrownMoneyDetail = listOf(
            ThrownMoneyDetail(
                thrownDate      = entity.createAt,
                thrownAmount    = entityOutMoney.sumBy { it.amount },
                takenAmount     = entity.amount,
                takenList       = takenListBuilder(entityOutMoney)
            )
        )

        logger.info(">>> thrownMoneyDetail :  $thrownMoneyDetail")

        responseService.success(SHOW_MONEY_OK, thrownMoneyDetail)
    } catch (e: Exception) {
        responseService.fail("$SHOW_MONEY_FAIL ${e.message}")
    }

    /**
     * 머니 뿌리기 조회 시 받은 아이디/머니 정보 생성
     *
     * @param outMoneyList 받은 자의 정보 리스트 객체
     * @return 받은 자의 [아이디, 머니] 리스트
     */
    private fun takenListBuilder(outMoneyList: List<OutMoney>): List<TakenList> {
        val resultList = mutableListOf<TakenList>()

        // 아이디와 머니 정보만 추출한다.
        for(i in 0..outMoneyList.size.minus(1)) {
            resultList.add(
                TakenList(
                    userId = outMoneyList[i].outUserId!!,
                    amount = outMoneyList[i].amount
                )
            )
        }

        return resultList.toList()
    }

    /**
     * 머니 뿌리기 시 받기 정보 초기화
     *
     * @param inMoney 머니 뿌리기 엔티티
     * @return Unit
     */
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

                // 머니 배분 과정에서 남는 머니가 발생하지 않도록 나머지 머니에 대한 처리를 한다.
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