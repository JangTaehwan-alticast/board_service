package com.msp.board_service.domain.response

import com.msp.board_service.domain.MultiLang
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable

/**
 * 60s * 60m * 24h * 7d
 */
@RedisHash(timeToLive = 10L)
data class BoardResponse(
    @Id
    var postId: String? = null,                     //게시글 아이디
    var nickName: String? = null,                   //게시글 작성자
    var category: String? = null,                   //카테고리
    var title: ArrayList<MultiLang>? = null,        //다국어 제목
    var contents: String? = null,                   //게시글 내용
    var createdDate: String? = null,                //작성일
    var lastUpdatedDate: String? = null            //마지막 업데이트일
):Serializable

data class InsertBoardResponse(
    var postId: String? = null,                     //입력한 게시글의 아이디
    var nickName: String? = null,                   //게시글 작성자
    var category: String? = null,                   //카테고리
    var title: ArrayList<MultiLang>? = null,        //다국어 제목
    var contents: String? = null,                   //게시글 내용
    var createdDate: String? = null                //게시글 작성일
)
@RedisHash(timeToLive = 10L)
data class BoardListResponse(
    @Id
    var postId: String? = null,                     //게시글 아이디
    var nickName: String? = null,                   //게시글 작성자
    var title: ArrayList<MultiLang>? = null,        //다국어 제목
    var category: String? = null,                   //카테고리
    var contents: String? = null,                   //게시글 내용
    var createdDate: String? = null                //게시글 작성일
):Serializable

data class CommentResponse(
    var postId: String? = null,                     //댓글 대상 게시글 아이디
    var commentId: String? = null,                  //댓글의 아이디
    var nickName: String? = null,                   //댓글 작성자
    var contents: String? = null,                   //댓글 내용
    var createdDate: String? = null,                //댓글 작성일
    var lastUpdatedDate: String? = null             //마지막 댓글 작성일
)
data class InsertCommentResponse(
    var postId: String? = null,                     //작성한 댓글 대상의 아이디
    var commentId: String? = null,                  //작성한 댓글의 아이디
    var nickName: String? = null,                   //댓글 작성자
    var contents: String? = null,                   //댓글 내용
    var createdDate: String? = null                //댓글 작성일
)