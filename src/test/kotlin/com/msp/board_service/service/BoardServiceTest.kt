package com.msp.board_service.service

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono

class BoardServiceTest {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun coroutineTest() {
        logger.info("Before runBlocking")
        runBlocking {
            logger.info("Log Before Task 1 Start")
            val currentTime = async {
                logger.info("Start First Task 1")
                delay(1000)
                logger.info("End First Task 1")
                System.currentTimeMillis()
            }
            val currentTime2 = async {
                logger.info("Start Second Task 2")
                delay(1000)
                logger.info("End Second Task2")
                System.currentTimeMillis()
            }
            logger.info("Task 1 = ${currentTime.await()}")
            logger.info("Task 2 = ${currentTime2.await()}")
            logger.info("Third Task 3")
            delay(1000)
            logger.info("Fourth Task 4")
            delay(1000)
        }
        logger.info("final Task??")
        runBlocking {
            logger.info("Second runBlocking")


            val second = async {
                delay(1000)
                logger.info("Second runBlocking Task1")
                32
            }
            logger.info("second = ${second.await()}")

            val third = async {
                delay(1000)
                logger.info("Second runBlocking Task2")
                64
            }
            logger.info("Second runBlocking Task3")
            logger.info("second = ${third.await()}")

        }

    }

    @Test
    fun webFluxTest(){
        val stopWatch = StopWatch("WebFlux Test")

        Mono.just("DATA").flatMap { it ->
            logger.info("it = $it")
            logger.info("Start Task1")
            stopWatch.start("Task1")
            Thread.sleep(100)
            stopWatch.stop()
            logger.info("End Task1")
            Mono.just("DATA2")
        }.flatMap {
            logger.info("Start Task2")
            stopWatch.start("Task2")
            Thread.sleep(100)
            stopWatch.stop()
            logger.info("End Task2")
            Mono.just("DATA3")
        }.flatMap {
            logger.info("Start Task3")
            stopWatch.start("Task3")
            Thread.sleep(100)
            stopWatch.stop()
            logger.info("End Task3")
            Mono.just("DATA3")
        }
        logger.info("${stopWatch.prettyPrint()}")
        logger.info("ELAPSE ${stopWatch.totalTimeMillis}ms.")

    }


    @Test
    fun regexTest(){
        var postId = "post_22324"
        val regex = Regex("^post_[0-9]*")
        val matches = postId.matches(regex)
        logger.info("matches result = $matches")
    }
}