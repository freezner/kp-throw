package com.freezner.service.kpthrow.repository.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "in_money")
class InMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var seq: Long = 0L

    @Column(name = "user_id")
    var userId: Long = 0L

    @Column(name = "room_id")
    var roomId: String? = ""

    var token: String = ""

    var amount: Int = 0

    @Column(name = "out_limit")
    var limit: Int = 0

    @Column(name = "create_at")
    var createAt: LocalDateTime = LocalDateTime.now()
}