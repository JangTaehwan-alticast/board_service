package com.msp.board_service.exception

class CustomException : Exception {
    var errorCode:Int = 0

    companion object{
        fun validation(message: String, value: String? = "", field: Any) = CustomException("$field : $message",1000005)
        fun invalidParameter(param:String) = CustomException("$param : 값이 누락되었습니다.",1000002)                            //파라미터 누락시
        fun invalidValueType() = CustomException("파라미터의 타입이 잘못되었습니다.",1000003)                                       //파라미터의 타입을 잘못 입력시
        fun invalidExpValue(exp: String) = CustomException("$exp : 잘못된 조건표현식입니다.",1000004)                             //검색 조건표현식 잘못 입력시

        fun invalidPostId(postId: String) =CustomException("$postId : 유효하지 않은 postId 입니다.",3260002)                     //유효하지 않는 postId 입력시
        fun invalidCommentId(commentId: String) =CustomException("$commentId : 유효하지 않은 commentId 입니다.",3260003)         //유효하지 않는 commentId 입력시
        fun invalidHistoryId(historyId: String) =CustomException("$historyId : 유효하지 않은 historyId 입니다.",3260004)         //유효하지 않는 historyId 입력시
        fun invalidSortField(field: String) =CustomException("$field : 유효하지 않은 정렬조건 입니다.",3260005)                    //유효하지 않는 sort field 입력시
        fun invalidSizeRange() = CustomException("size : 1..100 범위가 아닙니다.",3260006)                                      //유효하지 않는 size 범위
        fun invalidPageRange() = CustomException("page : -1 이상이어야 합니다.",3260007)                                         //유효하지 않는 page

    }

    constructor(message: String) : super(message)

    constructor(errorCode: Int):super(){
        this.errorCode = errorCode
    }

    constructor(message: String, errorCode: Int):super(message){
        this.errorCode = errorCode
    }

}