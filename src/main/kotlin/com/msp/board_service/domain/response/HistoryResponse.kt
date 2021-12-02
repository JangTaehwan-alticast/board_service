package com.msp.board_service.domain.response

import com.msp.board_service.domain.Board

data class ModHistoryListResponse(
    var historyId: String? = null,
    var postId: String? = null,
    var updatedDate: String? = null,
    var version: String? = null,
    var modifier: String? = null,
)

data class ModHistoryBoardResponse(
    var historyId: String? = null,              //수정 historyId
    var postId: String? = null,                 //원본글의 postId
    var type: String? = null,                   //히스토리로 이동된 메서드 (PATCH)
    var board: BoardResponse? = null,           //원본글
    var modifier: String? = null,               //수정한 사람
    var updatedDate: String? = null,            //수정 날짜
    var version: String? = null                 //해당 버전
)