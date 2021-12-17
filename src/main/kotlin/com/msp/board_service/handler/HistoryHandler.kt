package com.msp.board_service.handler

import com.msp.board_service.config.CodeConfig
import com.msp.board_service.exception.CustomException
import com.msp.board_service.response.Response
import com.msp.board_service.service.HistoryService
import com.msp.board_service.util.LogMessageMaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class HistoryHandler(val historyService: HistoryService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 수정 이력 리스트 조회
     */
    fun getModHistoryBoardList(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            historyService.getModHistoryBoardList(req.pathVariable("postId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "HistoryService",
                function = "getModHistoryBoardList",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.onErrorResume {
            when (it) {
                is CustomException -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "getModHistoryBoardList",
                        result = "FAILURE",
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode, it.message.toString(), null)))
                }
                else -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "getModHistoryBoardList",
                        result = "FAILURE",
                        message = it.message!!,
                        code = CodeConfig.UN_EXPECTED_EXCEPTION,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.message!!)))
                }
            }
        }
    }

    /**
     * 게시글 이전 버전 조회
     */
    fun getModHistoryBoard(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            historyService.getModHistoryBoard(req.pathVariable("historyId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "HistoryService",
                function = "getModHistoryBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.onErrorResume {
            when (it) {
                is CustomException -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "getModHistoryBoard",
                        result = "FAILURE",
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode, it.message.toString(), null)))
                }
                else -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "getModHistoryBoard",
                        result = "FAILURE",
                        message = it.message!!,
                        code = CodeConfig.UN_EXPECTED_EXCEPTION,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.message!!)))
                }
            }
        }
    }

    /**
     * 삭제된 게시글 복원
     */
    fun restoreDeletedBoard(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            historyService.restoreDeletedBoard(req.pathVariable("postId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "HistoryService",
                function = "restoreDeletedBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.deletedCount)))
        }.onErrorResume {
            when (it) {
                is CustomException -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "restoreDeletedBoard",
                        result = "FAILURE",
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode, it.message.toString(), null)))
                }
                else -> {
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "HistoryService",
                        function = "restoreDeletedBoard",
                        result = "FAILURE",
                        message = it.message!!,
                        code = CodeConfig.UN_EXPECTED_EXCEPTION,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.message!!)))
                }
            }
        }
    }
}