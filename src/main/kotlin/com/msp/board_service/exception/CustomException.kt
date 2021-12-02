package com.msp.board_service.exception

class CustomException : Exception {
    var errorCode:Int = 0

    companion object{
        fun invalidParameter(param:String) = CustomException("$param is required", 1000002)                             //파라미터 누락시
        fun invalidValueType() = CustomException("not a valid value type",1000003)                                      //파라미터의 타입을 잘못 입력시
        fun invalidExpValue(exp: String) =CustomException("$exp is not a valid Predicates",1000004)                     //검색 조건표현식 잘못 입력시
        fun exceedMaxValue(field:String,param:String,length:Int) =                                                      //필드 최대값 초과시
            CustomException("$field($param) exceeds the maximum value.(max: $length Characters include blank)",3260001)
        fun invalidPostId(postId: String) =CustomException("$postId is not a valid postId",3260002)                     //유효하지 않는 postId 입력시
        fun invalidCommentId(commentId: String) =CustomException("$commentId is not a valid commentId",3260003)         //유효하지 않는 commentId 입력시
        fun invalidHistoryId(historyId: String) =CustomException("$historyId is not a valid historyId",3260004)         //유효하지 않는 historyId 입력시
    }

    constructor(message: String) : super(message)

    constructor(errorCode: Int):super(){
        this.errorCode = errorCode
    }

    constructor(message: String, errorCode: Int):super(message){
        this.errorCode = errorCode
    }

}