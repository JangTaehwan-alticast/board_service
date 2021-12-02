package com.msp.board_service.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class CommonService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object{
        fun stringToEpoch(time: String):Long{
            var date = SimpleDateFormat("yyyy-MM-dd").parse(time)
            return date.time
        }

        fun epochToString(time: Long):String{
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(time *1000)
        }

        fun getNowEpochTime():Long{
            return LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
        }
    }




}