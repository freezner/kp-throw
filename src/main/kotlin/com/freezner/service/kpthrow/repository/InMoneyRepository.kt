package com.freezner.service.kpthrow.repository

import com.freezner.service.kpthrow.repository.entity.InMoney
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InMoneyRepository : JpaRepository<InMoney, Long> {
    fun findByToken(token: String): InMoney
}