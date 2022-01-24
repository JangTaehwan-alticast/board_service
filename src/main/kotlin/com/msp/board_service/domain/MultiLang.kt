package com.msp.board_service.domain

import java.io.Serializable

data class MultiLang(
    var lang: String,               //언어
    var value: String               //값
):Serializable

