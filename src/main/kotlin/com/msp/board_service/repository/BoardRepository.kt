package com.msp.board_service.repository

import com.msp.board_service.domain.BoardTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class BoardRepository(private val template: ReactiveMongoTemplate) {

    companion object{
        const val COLLECTION_NM = "boards"
    }

    fun insertBoard(board: BoardTest) : Mono<BoardTest> {
        return template.insert(board, COLLECTION_NM)
    }

}