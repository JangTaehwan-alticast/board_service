package com.msp.board_service.domain.request

import com.msp.board_service.domain.MultiLang

data class ModBoardRequest(
    var title : ArrayList<MultiLang>? = null,       //수정할 다국어 제목
    var contents : String? = null,                  //수정할 게시글 내용
    var modifier : String? = null                   //수정한 사람
)

data class ModCommentRequest(
    var contents: String? = null                    //댓글 수정 내용
)

data class InsertCommentRequest(
    var nickName: String? = null,                   //댓글 작성자
    var contents: String? = null                    //댓글 내용
)

