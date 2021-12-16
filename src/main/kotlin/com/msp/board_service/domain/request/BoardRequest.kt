package com.msp.board_service.domain.request

import com.msp.board_service.domain.MultiLang
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.*

data class InsertBoardRequest(
    @field:Length(min = 2, max = 20)
    @field:NotBlank(message = "닉네임을 입력하세요.")
    var nickName: String? = null,                   //작성자 닉네임

    @field:NotEmpty
    var title: ArrayList<MultiLang>,                //제목 (다국어)

    @field:Length(max = 255)
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String? = null,                   //게시글 내용
    var category: String? = null,                   //카테고리 default all
    var exposureDate: Long? =  null,                //노출 예정 시각

)

data class ModBoardRequest(
    var title : ArrayList<MultiLang>? = null,       //수정할 다국어 제목

    @field:Length(max = 255)
    var contents : String? = null,                  //수정할 게시글 내용

    @field:Length(min = 2, max = 20)
    @field:NotBlank(message = "닉네임을 입력하세요.")
    var modifier : String? = null                   //수정한 사람
)

data class ModCommentRequest(
    @field:Length(max = 255)
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String? = null                    //댓글 수정 내용
)

data class InsertCommentRequest(
    @field:Length(min = 2, max = 20)
    @field:NotBlank(message = "닉네임을 입력하세요.")
    var nickName: String? = null,                   //댓글 작성자

    @field:Length(max = 255)
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String? = null                    //댓글 내용
)

