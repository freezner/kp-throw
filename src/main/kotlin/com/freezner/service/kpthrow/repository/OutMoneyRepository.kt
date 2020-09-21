package com.freezner.service.kpthrow.repository

import com.freezner.service.kpthrow.repository.entity.OutMoney
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OutMoneyRepository: JpaRepository<OutMoney, Long> {
    fun findAllByToken(token: String): List<OutMoney>

    fun findAllByTokenAndOutUserIdIsNotNull(token: String): List<OutMoney>
}