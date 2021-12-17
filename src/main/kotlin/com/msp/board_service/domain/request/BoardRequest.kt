package com.msp.board_service.domain.request

import com.msp.board_service.common.customValidation.TitleValidation
import com.msp.board_service.domain.MultiLang
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.*

data class InsertBoardRequest(

    @field:Length(min = 2, max = 20, message = "길이가 2에서 20 사이여야 합니다(공백포함)")
    @field:NotEmpty(message = "닉네임을 입력하세요.")
    var nickName: String,                                                       //작성자 닉네임

    @field:TitleValidation
    var title: ArrayList<MultiLang>? = null,                                    //제목 (다국어)

    @field:Length(max = 255, message = "길이가 0에서 255 사이여야 합니다(공백포함)")
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String,                                                       //게시글 내용
    var category: String? = null,                                               //카테고리 default all
    var exposureDate: Long? =  null,                                            //노출 예정 시각

)

data class ModBoardRequest(

    var title : ArrayList<MultiLang>? = null,                                   //수정할 다국어 제목

    @field:Length(max = 255, message = "길이가 0에서 255 사이여야 합니다(공백포함)")
    var contents : String? = null,                                              //수정할 게시글 내용

    @field:Length(min = 2, max = 20, message = "길이가 2에서 20 사이여야 합니다(공백포함)")
    @field:NotBlank(message = "닉네임을 입력하세요.")
    var modifier : String                                                       //수정한 사람
)

data class ModCommentRequest(
    @field:Length(max = 255, message = "길이가 0에서 255 사이여야 합니다(공백포함)")
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String                                                        //댓글 수정 내용
)

data class InsertCommentRequest(
    @field:Length(min = 2, max = 20, message = "길이가 2에서 20 사이여야 합니다(공백포함)")
    @field:NotBlank(message = "닉네임을 입력하세요.")
    var nickName: String,                                                       //댓글 작성자

    @field:Length(max = 255, message = "길이가 0에서 255 사이여야 합니다(공백포함)")
    @field:NotBlank(message = "내용을 입력하세요.")
    var contents: String                                                        //댓글 내용
)

