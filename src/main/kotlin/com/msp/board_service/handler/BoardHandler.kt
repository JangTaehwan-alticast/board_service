package com.msp.board_service.handler

import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.config.ErrorCode
import com.msp.board_service.domain.Board
import com.msp.board_service.response.Response
import com.msp.board_service.exception.CustomException
import com.msp.board_service.service.BoardService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
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

    @LogExecute(level = LoggerLevel.INFO, message = "메소드 호출")
    fun getBoardList(req:ServerRequest) : Mono<ServerResponse> {
        var query = req.queryParam("postId")
        var result = Response(ErrorCode.CODE_OK, ErrorCode.MESSAGE_OK, query)
        return ServerResponse.ok().body(Mono.just(result)).switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND).build())
    }

//    @LogExecute(level = LoggerLevel.INFO, message = "메소드 호출")
    fun insertBoard(req:ServerRequest) : Mono<ServerResponse> {
        return req.bodyToMono(Board::class.java).switchIfEmpty {
            throw CustomException.invalidParameter("body")
        }.flatMap { board ->
            this.boardService.insertBoard(board)
        }.flatMap {
            ok().body(Mono.just(Response.ok(it)))
        }.onErrorResume {
            logger.error(it.message.toString()) // log formatter 어디서 / 뭐가 / 왜 / 소요시간
            when(it){
                is CustomException ->{
                    ok().body(Mono.just(Response(it.errorCode,it.message.toString(),null)))
                }else ->{
                    ok().body(Mono.just(Response.unExpectedException(it.message.toString())))
                }
            }

        }
    }
}