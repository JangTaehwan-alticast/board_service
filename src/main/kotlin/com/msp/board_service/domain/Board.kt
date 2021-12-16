package com.msp.board_service.domain

import com.msp.board_service.util.CommonService
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import javax.validation.constraints.NotBlank

@Document("board")
data class Board(
    var postId: String? = null,                 //게시글의 아이디
    var nickName: String? = null,               //게시글 작성자
    var title: ArrayList<MultiLang>? = null,    //다국어 제목
    var contents: String? = null,               //게시글 내용
    var category: String? = null,               //카테고리
    var useYn: String? = null,                  //노출/사용 여부
    var exposureDate: Long? = null,             //노출 예정 시각
    var createdDate: Long? = null,              //글 작성일
    var lastUpdatedDate: Long? = null,          //마지막 업데이트일
)


@Document("comment")
data class Comment(
    var postId: String? = null,                 //댓글 대상 게시글의 아이디
    var commentId: String? = null,              //댓글의 아이디
    var nickName: String? = null,               //댓글 작성자
    var contents: String? = null,               //댓글 내용
    var createdDate: Long? = null,              //댓글 작성일
    var lastUpdatedDate: Long? = null,          //마지막 업데이트일
)

