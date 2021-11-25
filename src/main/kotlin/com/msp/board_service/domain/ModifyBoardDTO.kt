package com.msp.board_service.domain

data class ModifyBoardDTO(
    var title : ArrayList<MultiLang>? = null,
    var contents : String? = null,
    var modifier : String? = null
)