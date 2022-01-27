package com.msp.board_service.config

object CodeConfig {
    const val CODE_OK = 200                             // ok
    const val INVALID_VALUE_TYPE = 1000003
    const val UN_EXPECTED_EXCEPTION = 1000006

    const val MESSAGE_OK = "OK"                         // ok message
    const val MESSAGE_UNEXPECTED = "Internal server error"
    const val MESSAGE_NO_VALUE = "No value present"
}

object LangCodeConfig{
    val LANG_CODE_SET = setOf("ar", "zh", "en", "de", "fr", "ja", "ko", "ru")
}

object RedisKey{
    const val LATEST_BOARD = "latestBoard"
    const val TOTAL = "total"
    const val SINGLE_BOARD = "singleBoard"
}


object DefaultCode{
    const val FALLBACK_LANG = "en"
}
