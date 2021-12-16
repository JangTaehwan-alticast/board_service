package com.msp.board_service.handler

import com.msp.board_service.config.CodeConfig
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.request.InsertBoardRequest
import com.msp.board_service.domain.request.ModBoardRequest
import com.msp.board_service.exception.CustomException
import com.msp.board_service.response.Response
import com.msp.board_service.service.BoardService
import com.msp.board_service.util.LogMessageMaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import javax.validation.Validation
import javax.xml.validation.Validator

@Component
class BoardHandler(val boardService: BoardService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 게시판 글 검색 포함 목록
     */
    fun getBoardList(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            boardService.getBoardList(
                postId = req.queryParam("postId").orElse(""),
                category = req.queryParam("category").orElse(""),
                nickName = req.queryParam("nickName").orElse(""),
                title = req.queryParam("title").orElse(""),
                contents = req.queryParam("contents").orElse(""),
                q = req.queryParam("q").orElse(""),
                page = req.queryParam("page").orElse("0").toLong(),
                size = req.queryParam("size").orElse("0").toLong(),
                orderBy = req.queryParam("orderBy").orElse(""),
                lang = req.queryParam("lang").orElse("")
            )
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "getBoardList",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.switchIfEmpty {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "getBoardList",
                result = "SUCCESS",
                value = CodeConfig.MESSAGE_NO_VALUE,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "getBoardList",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "getBoardList",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 500,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.cause.toString())))
                }
            }
        }
    }

    /**
     * 게시글 단건 조회
     */
    fun getOneBoard(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            boardService.getOneBoard(req.pathVariable("postId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "getOneBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "getOneBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "getOneBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 500,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.cause.toString())))
                }
            }
        }
    }

    /**
     * 게시글 입력
     */
    fun insertBoard(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(InsertBoardRequest::class.java).flatMap { insertBoardRequest ->
            val validator = Validation.buildDefaultValidatorFactory().validator.validate(insertBoardRequest)
            if(validator.isNotEmpty())
                return@flatMap Mono.error(CustomException.validation(
                    message = validator.first().message,
                    field = validator.first().propertyPath
                ))
            this.boardService.insertBoard(insertBoardRequest)
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "insertBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.switchIfEmpty {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "insertBoard",
                result = "SUCCESS",
                value = CodeConfig.MESSAGE_NO_VALUE,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "insertBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }
                is ServerWebInputException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "insertBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 400,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(
                        CodeConfig.INVALID_VALUE_TYPE,CustomException.invalidValueType().message,null)))
                }
                else ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "insertBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 500,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.cause.toString())))
                }
            }
        }
    }


    /**
     * 게시글 수정
     */
    fun modifyBoard(req:ServerRequest):Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(ModBoardRequest::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap { modBoardRequest ->
            val validator = Validation.buildDefaultValidatorFactory().validator.validate(modBoardRequest)
            if(validator.isNotEmpty())
                return@flatMap Mono.error(CustomException.validation(
                    message = validator.first().message,
                    field = validator.first().propertyPath
                ))
            boardService.modifyBoard(req.pathVariable("postId"),modBoardRequest)
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "updateBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.modifiedCount)))
        }.switchIfEmpty {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "updateBoard",
                result = "SUCCESS",
                value = CodeConfig.MESSAGE_NO_VALUE,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "updateBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "updateBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 500,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.cause.toString())))
                }
            }
        }
    }
    /**
     * 게시글 삭제
     */
    fun deleteBoard(req:ServerRequest):Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            boardService.deleteBoard(it.pathVariable("postId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "deleteBoard",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.deletedCount)))
        }.switchIfEmpty {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "BoardService",
                function = "deleteBoard",
                result = "SUCCESS",
                value = CodeConfig.MESSAGE_NO_VALUE,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "deleteBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = it.errorCode,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "BoardService",
                        function = "deleteBoard",
                        result = "FAILURE" ,
                        message = it.message!!,
                        code = 500,
                        path = req.pathVariables(),
                        param = req.queryParams()
                    )
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(it.cause.toString())))
                }
            }
        }
    }
}