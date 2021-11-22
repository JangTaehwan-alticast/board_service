package com.msp.board_service.domain

import org.springframework.data.mongodb.core.mapping.Document

@Document("boardTests")
data class BoardTest(
    var boardTestId: String? = null,
    var title: String? = null,
    var contents: String? = null
)
