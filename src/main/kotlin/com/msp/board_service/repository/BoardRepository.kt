package com.msp.board_service.repository

import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.BoardTest
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono

@Repository
class BoardRepository(private val template: ReactiveMongoTemplate) {


    companion object{
        const val COLLECTION_NM = "boards"
    }

    @LogExecute(level = LoggerLevel.INFO, message = "메소드 호출")
    fun insertBoard(board: Board) : Mono<Board> {
        return template.insert(board, COLLECTION_NM)
    }

}