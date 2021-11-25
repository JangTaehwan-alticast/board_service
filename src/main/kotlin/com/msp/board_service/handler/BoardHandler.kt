package com.msp.board_service.handler

import com.msp.board_service.config.CodeConfig
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.ModifyBoardDTO
import com.msp.board_service.exception.CustomException
import com.msp.board_service.response.Response
import com.msp.board_service.service.BoardService
import com.msp.board_service.util.LogMessageMaker
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

@Component
class BoardHandler(val boardService: BoardService) {

    private val logger = KotlinLogging.logger {  }

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
                size = req.queryParam("size").orElse("0").toLong()
            )
        }.flatMap {
            var logMsg = LogMessageMaker.getSuccessLog(stopWatch,"BoardService","getBoardList","SUCCESS",it)
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.switchIfEmpty {
            var logMsg = LogMessageMaker.getSuccessLog(stopWatch,"BoardService","getBoardList","SUCCESS",CodeConfig.MESSAGE_NO_VALUE)
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "getBoardList","FAILURE" ,it.message!!, it.errorCode)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "getBoardList","FAILURE" ,it.message!!, 500)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(CodeConfig.MESSAGE_UNEXPECTED)))
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
        return req.bodyToMono(Board::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap {
            this.boardService.insertBoard(it)
        }.flatMap {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "insertBoard", "SUCCESS", it)
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.switchIfEmpty{
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "insertBoard", "SUCCESS", CodeConfig.MESSAGE_NO_VALUE)
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "insertBoard","FAILURE" ,it.message!!, it.errorCode)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "insertBoard","FAILURE" ,it.message!!, 500)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException(CodeConfig.MESSAGE_UNEXPECTED)))
                }
            }

        }
    }


    /**
     * 게시글 수정
     * pathVariable 과 body가 있어야 함.
     */
    fun modifyBoard(req:ServerRequest):Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(ModifyBoardDTO::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap {
            boardService.updateBoard(req.pathVariable("postId"),it)
        }.flatMap {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "modifyBoard", "SUCCESS", it)
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.modifiedCount)))
        }.switchIfEmpty {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "modifyBoard", "SUCCESS", "No value present")
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "modifyBoard","FAILURE" ,it.message!!, it.errorCode)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "modifyBoard","FAILURE" ,it.message!!, 500)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response.unExpectedException("INTERNAL_SERVER_ERROR")))
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
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "deleteBoard", "SUCCESS", it)
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it.deletedCount)))
        }.switchIfEmpty {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "deleteBoard", "SUCCESS", "No value present")
            logger.info(logMsg)
            ok().body(Mono.just(Response.noValuePresent()))
        }.onErrorResume {
            when(it){
                is CustomException ->{
                    val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "deleteBoard","FAILURE" ,it.message!!, it.errorCode)
                    logger.error(logMsg)
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                val logMsg = LogMessageMaker.getFailureLog(stopWatch, "BoardService", "deleteBoard","FAILURE" ,it.message!!, 500)
                logger.error(logMsg)
                ok().body(Mono.just(Response.unExpectedException("INTERNAL_SERVER_ERROR")))
            }
            }
        }
    }
}