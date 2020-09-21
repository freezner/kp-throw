package com.freezner.service.kpthrow

import com.fasterxml.jackson.databind.ObjectMapper
import com.freezner.service.kpthrow.domain.RequestInMoney
import com.freezner.service.kpthrow.repository.InMoneyRepository
import com.freezner.service.kpthrow.repository.OutMoneyRepository
import com.freezner.service.kpthrow.service.InMoneyService
import mu.KLogging
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.time.LocalDateTime

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureMockMvc
class KpThrowApplicationTests(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val inMoneyRepository: InMoneyRepository,
	private val outMoneyRepository: OutMoneyRepository,
	private val inMoneyService: InMoneyService
) {
	companion object: KLogging() {
		private const val USER_ID: Long = 10001
		private const val ROOM_ID: String = "abcdef"
		private const val THROW_AMOUNT: Int = 12500
		private const val LIMIT: Int = 5
	}

	@DisplayName("뿌리기 테스트")
	@Test
	fun inMoneyTest() {
		val params = RequestInMoney(
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		mockMvc.post("/api/v1/splash") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", USER_ID)
			header("X-ROOM-ID", ROOM_ID)
			content = objectMapper.writeValueAsString(params)
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
		}
	}

	@DisplayName("받기 테스트")
	@Test
	fun outMoneyTest00() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, USER_ID, ROOM_ID)

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10002)
			header("X-ROOM-ID", ROOM_ID)
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("success"){ value("true") }
		}
		.andDo { print() }
	}

	@DisplayName("뿌리기 당 한 사용자는 한 번만 받을 수 있습니다.")
	@Test
	fun outMoneyTest01() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10003, "A10003")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 같은 사용자로 두번 받기 시도 한다.
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10004)
			header("X-ROOM-ID", "A10003")
		}

		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10004)
			header("X-ROOM-ID", "A10003")
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("errorCode"){ value("2001") }
		}
		.andDo { print() }
	}

	@DisplayName("자신이 뿌리기 한 건은 자신이 받을 수 없습니다.")
	@Test
	fun outMoneyTest02() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10005, "A10005")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 뿌린 아이디로 받기 시도
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10005)
			header("X-ROOM-ID", "A10005")
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("errorCode"){ value("2003") }
		}
		.andDo { print() }
	}

	@DisplayName("뿌린이와 동일한 채팅방에 있는 사용자만 받기 가능")
	@Test
	fun outMoneyTest03() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10006, "A10006")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 받는 아이디의 방ID를 바꿔본다.
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10007)
			header("X-ROOM-ID", "A10005")
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("errorCode"){ value("2004") }
		}
		.andDo { print() }
	}

	@DisplayName("뿌린 건은 10분간 유효합니다.")
	@Test
	fun outMoneyTest04() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10008, "A10008")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 생성된 뿌리기 건의 생성 시간을 10분 전으로 돌린다.
		val entity = outMoneyRepository.findAllByToken(token)
		entity.forEach {
			it.createAt = LocalDateTime.now().minusMinutes(10)
		}

		outMoneyRepository.saveAll(entity)

		// 받는 아이디의 방ID를 바꿔본다.
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10009)
			header("X-ROOM-ID", "A10008")
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("errorCode"){ value("2005") }
		}
		.andDo { print() }
	}

	@DisplayName("뿌린 사람 자신만 조회할 수 있습니다.")
	@Test
	fun outMoneyTest05() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10010, "A10010")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 첫번째 받음
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10011)
			header("X-ROOM-ID", "A10010")
		}

		// 두번째 받음
		mockMvc.put("/api/v1/take/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10012)
			header("X-ROOM-ID", "A10010")
		}

		// 조회 아이디를 변경한다.
		mockMvc.get("/api/v1/info/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10011)
			header("X-ROOM-ID", "A10010")
		}
		.andExpect {
			status().isOk
			contentType(APPLICATION_JSON)
			jsonPath("errorCode"){ value("3003") }
		}
		.andDo { print() }
	}

	@DisplayName("뿌린 건에 대한 조회는 7일 동안 할 수 있습니다.")
	@Test
	fun outMoneyTest06() {
		val requestDto = RequestInMoney(
			userId = 0L,
			roomId = "",
			amount = THROW_AMOUNT,
			limit = LIMIT
		)

		inMoneyService.setInMoney(requestDto, 10012, "A10012")

		val token = inMoneyRepository.findTop1ByOrderBySeqDesc().token

		// 생성된 뿌리기 건의 생성 시간을 7일 후로 돌린다.
		val entity = inMoneyRepository.findByToken(token)
		entity.createAt = LocalDateTime.now().minusDays(7)

		// 조회 아이디를 변경한다.
		mockMvc.get("/api/v1/info/${token}") {
			contentType = APPLICATION_JSON
			accept = APPLICATION_JSON
			header("X-USER-ID", 10012)
			header("X-ROOM-ID", "A10012")
		}
			.andExpect {
				status().isOk
				contentType(APPLICATION_JSON)
				jsonPath("errorCode"){ value("3002") }
			}
			.andDo { print() }
	}
}