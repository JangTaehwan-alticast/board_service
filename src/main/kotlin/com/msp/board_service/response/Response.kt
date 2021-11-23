package com.msp.board_service.response


class Response(
    var code: Int? = 0,
    var message: String? = null,
    var result: Any? = null
){
    companion object{
        fun ok(result:Any) = Response(200, "OK",result)
        fun noValuePresent() = Response(1000001,"No value present", null)
        fun unExpectedException(message:String) = Response(500,message,null)
    }
}