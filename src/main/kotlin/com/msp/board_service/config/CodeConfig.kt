package com.msp.board_service.config

object CodeConfig {
    const val CODE_OK = 200                             // ok
    const val INVALID_VALUE_TYPE = 1000003

    const val MESSAGE_OK = "OK"                         // ok message
    const val MESSAGE_UNEXPECTED = "Internal server error"
    const val MESSAGE_NO_VALUE = "No value present"
}

object DefaultCode{
    const val FALLBACK_LANG = "en"
}