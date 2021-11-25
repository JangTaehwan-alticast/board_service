package com.msp.board_service.response

import com.msp.board_service.config.CodeConfig


class Response(
    var code: Int? = 0,
    var message: String? = null,
    var result: Any? = null
){
    companion object{
        fun ok(result:Any) = Response(200, CodeConfig.MESSAGE_OK,result)
        fun noValuePresent() = Response(1000001,CodeConfig.MESSAGE_NO_VALUE, null)
        fun unExpectedException(message:String) = Response(500,message,null)
    }
}