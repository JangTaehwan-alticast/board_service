package com.msp.board_service.domain

import com.msp.board_service.domain.response.BoardResponse
import org.springframework.data.mongodb.core.mapping.Document

@Document("history")
data class ModifyBoardHistory(
    var historyId: String? = null,              //수정 historyId
    var postId: String? = null,                 //원본글의 postId
    var type: String? = null,                   //히스토리로 이동된 메서드 (PATCH)
    var board: BoardResponse? = null,           //원본글
    var modifier: String? = null,               //수정한 사람
    var updatedDate: Long? = 0,                 //수정 날짜 (epoch Time)
    var version: String? = null                 //해당 버전 (oldVer)
)
@Document("history")
data class DeleteBoardHistory(
    var historyId: String? = null,              //삭제 historyId
    var postId: String? = null,                 //삭제된 글의 postId
    var type: String? = null,                   //히스토리로 이동된 메서드 (DELETE)
    var board: Board? = null,                   //삭제된 원본글
    var comment: ArrayList<Comment>? = null,    //삭제된 원본글의 댓글 목록
    var deletedDate: Long? = 0,                 //삭제된 날짜
)


