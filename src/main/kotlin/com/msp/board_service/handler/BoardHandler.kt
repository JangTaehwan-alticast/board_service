package com.msp.board_service.handler

import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.config.ErrorCode
import com.msp.board_service.domain.BoardTest
import com.msp.board_service.domain.Response
import com.msp.board_service.service.BoardService
import mu.KotlinLogging
import org.jetbrains.kotlin.com.google.common.base.Stopwatch
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
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

    fun insertBoardTest(req:ServerRequest) : Mono<ServerResponse> {
        var msg = ""
        return req.bodyToMono(BoardTest::class.java).flatMap { board ->
            msg = board.toString()
            this.boardService.insertBoardTest(board)
        }.flatMap {
            ok().body(Mono.just("ok"))
        }

    }
}