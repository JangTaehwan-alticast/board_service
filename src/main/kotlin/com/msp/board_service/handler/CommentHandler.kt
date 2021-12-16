package com.msp.board_service.handler

import com.msp.board_service.config.CodeConfig
import com.msp.board_service.domain.request.InsertCommentRequest
import com.msp.board_service.domain.request.ModCommentRequest
import com.msp.board_service.exception.CustomException
import com.msp.board_service.response.Response
import com.msp.board_service.service.CommentService
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

@Component
class CommentHandler(val cmntService: CommentService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 댓글 리스트 조회
     */
    fun findCmntList(req:ServerRequest): Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            cmntService.findCmntList(
                postId = req.pathVariable("postId"),
                page = req.queryParam("page").orElse("0").toLong(),
                size = req.queryParam("size").orElse("0").toLong()
            )
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "CommentService",
                function = "getCmntList",
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
                serviceName = "CommentService",
                function = "getCmntList",
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
                        serviceName = "CommentService",
                        function = "getCmntList",
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
                        serviceName = "CommentService",
                        function = "getCmntList",
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
     * 댓글 입력
     */
    fun insertCmnt(req:ServerRequest): Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(InsertCommentRequest::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap { insertCmntRequest ->
            val validator = Validation.buildDefaultValidatorFactory().validator.validate(insertCmntRequest)
            if(validator.isNotEmpty())
                return@flatMap Mono.error(CustomException.validation(
                    message = validator.first().message,
                    field = validator.first().propertyPath
                ))
            cmntService.insertCmnt(req.pathVariable("postId"),insertCmntRequest)
        }.flatMap { 
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "CommentService",
                function = "insertCmnt",
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
                        serviceName = "CommentService",
                        function = "insertCmnt",
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
                        serviceName = "CommentService",
                        function = "insertCmnt",
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
                        serviceName = "CommentService",
                        function = "insertCmnt",
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
     * 댓글 삭제
     */
    fun deleteCmnt(req:ServerRequest): Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            cmntService.deleteCmnt(req.pathVariable("commentId"))
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "CommentService",
                function = "deleteCmnt",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.deletedCount)))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "CommentService",
                        function = "deleteCmnt",
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
                        serviceName = "CommentService",
                        function = "deleteCmnt",
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
     * 댓글 수정
     */
    fun modifyCmnt(req:ServerRequest): Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(ModCommentRequest::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap { modCmntRequest ->
            val validator = Validation.buildDefaultValidatorFactory().validator.validate(modCmntRequest)
            if(validator.isNotEmpty())
                return@flatMap Mono.error(CustomException.validation(
                    message = validator.first().message,
                    field = validator.first().propertyPath
                ))
            cmntService.modifyCmnt(req.pathVariable("commentId"),modCmntRequest)
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(
                stopWatch = stopWatch,
                serviceName = "CommentService",
                function = "modifyCmnt",
                result = "SUCCESS",
                value = it,
                path = req.pathVariables(),
                param = req.queryParams()
            )
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.modifiedCount)))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(
                        stopWatch = stopWatch,
                        serviceName = "CommentService",
                        function = "modifyCmnt",
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
                        serviceName = "CommentService",
                        function = "modifyCmnt",
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