package com.msp.board_service.aop
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.System.*

@Aspect
@Component
class LogAspect {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
    @Around("@annotation(logExecute)")
    fun log(joinPoint: ProceedingJoinPoint, logExecute: LogExecute): Any? =
        joinPoint.run {
            val start = currentTimeMillis()
            joinPoint.proceed()
                .also {
                      logMessage(start = start, signature = joinPoint.signature.toShortString(), message = logExecute.message, level = logExecute.level)
                }
        }

    fun logMessage(start: Long, signature: String, api: String?="", message: String?="",level: LoggerLevel){

        val logMessage = with(StringBuffer()){
            append("[Board-service AOP] $signature time: ${currentTimeMillis()-start} ms")
            if(message != null && message.isNotBlank()) append(", message: $message")
            toString()
        }
        when(level){
            LoggerLevel.TRACE -> logger.trace(logMessage)
            LoggerLevel.DEBUG -> logger.debug(logMessage)
            LoggerLevel.INFO -> logger.info(logMessage)
            LoggerLevel.WARN -> logger.warn(logMessage)
            LoggerLevel.ERROR -> logger.error(logMessage)
        }
    }


}