package com.msp.board_service.repository

import com.mongodb.client.result.DeleteResult
import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.domain.Board
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class BoardRepository(private val template: ReactiveMongoTemplate) {


    companion object{
        const val COLLECTION_NM = "boards"
    }

    fun insertBoard(board: Board) : Mono<Board> {
        return template.insert(board, COLLECTION_NM)
    }

    fun deleteBoard(query: Query): Mono<DeleteResult> {
        return template.remove(query, COLLECTION_NM)
    }

    fun findOneBoard(query:Query):Mono<Board> {
        return template.findOne(query,Board::class.java)
    }

}