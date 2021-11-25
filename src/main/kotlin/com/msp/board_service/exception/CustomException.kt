package com.msp.board_service.exception

class CustomException : Exception {
    var errorCode:Int = 0

    companion object{
        fun invalidParameter(param:String) = CustomException("$param is required", 1000002)
        fun exceedMaxValue(field:String,param:String,length:Int) = CustomException("$field($param) exceeds the maximum value.(max: $length Characters include blank)",3260001)
    }

    constructor(message: String) : super(message)

    constructor(errorCode: Int):super(){
        this.errorCode = errorCode
    }

    constructor(message: String, errorCode: Int):super(message){
        this.errorCode = errorCode
    }

}