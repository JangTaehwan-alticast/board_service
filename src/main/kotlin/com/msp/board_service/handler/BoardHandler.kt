package com.msp.board_service.handler

import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.config.ErrorCode
import com.msp.board_service.domain.Board
import com.msp.board_service.response.Response
import com.msp.board_service.exception.CustomException
import com.msp.board_service.service.BoardService
import com.msp.board_service.util.LogMessageMaker
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

import java.lang.IllegalArgumentException

@Component
class BoardHandler(val boardService: BoardService) {

    private val logger = KotlinLogging.logger {  }

    fun getBoardList(req:ServerRequest) : Mono<ServerResponse> {
        var query = req.queryParam("postId")
        var result = Response(ErrorCode.CODE_OK, ErrorCode.MESSAGE_OK, query)
        return ServerResponse.ok().body(Mono.just(result)).switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND).build())
    }

    /**
     * 게시글 입력
     */
    fun insertBoard(req:ServerRequest) : Mono<ServerResponse> {
        val stopWatch = StopWatch()
        stopWatch.start()
        return req.bodyToMono(Board::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap { board ->
            this.boardService.insertBoard(board)
        }.flatMap {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "insertBoard", "SUCCESS", it)
            logger.info(logMsg)
            ok().body(Mono.just(Response.ok(it)))
        }.switchIfEmpty{
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "insertBoard", "SUCCESS", "No value present")
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
                    ok().body(Mono.just(Response.unExpectedException("INTERNAL_SERVER_ERROR")))
                }
            }

        }
    }

    /**
     * 게시글 삭제
     */
    // TODO: 2021/11/24 postId 입력받아 삭제처리 deletedDate / useYn update
    fun deleteBoard(req:ServerRequest):Mono<ServerResponse>{
        val stopWatch = StopWatch()
        stopWatch.start()
        return Mono.just(req).flatMap {
            logger.info("pathVariable=${it.pathVariable("postId")}")
            boardService.deleteBoard(it.pathVariable("postId"))
        }.flatMap {
            val logMsg = LogMessageMaker.getSuccessLog(stopWatch, "BoardService", "deleteBoard", "SUCCESS", it.deletedCount)
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

        /**
         * 게시글 수정
         */
        

    }
}