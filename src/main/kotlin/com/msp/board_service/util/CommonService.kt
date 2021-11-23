package com.msp.board_service.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
class CommonService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun stringToEpoch(time: String):Long{
        var date = SimpleDateFormat("yyyy-MM-dd").parse(time)
        logger.info("epochTime=${date.time}")
        return date.time
    }

    fun epochToString(time: Long):String{
        return SimpleDateFormat("yyyy-MM-dd`T'HH:mm:ss.SSSz").format(Date(time!!))
    }


}