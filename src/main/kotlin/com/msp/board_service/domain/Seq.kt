package com.msp.board_service.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("seq")
data class Seq(
    @Id
    var id: String,
    var seq: Long
)
