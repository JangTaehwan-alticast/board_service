package com.msp.board_service.aop


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class LogExecute(
    val level: LoggerLevel = LoggerLevel.INFO,
    val message: String = ""
)
