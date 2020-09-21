package com.freezner.service.kpthrow.repository.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "out_money")
class OutMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var seq: Long = 0L

    var token: String = ""

    @Column(name = "room_id")
    var roomId: String = ""

    @Column(name = "in_user_id")
    var inUserId: Long = 0L

    @Column(name = "out_user_id")
    var outUserId: Long? = null

    var amount: Int = 0

    @Column(name = "create_at")
    var createAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "update_at")
    var updateAt: LocalDateTime? = null
}