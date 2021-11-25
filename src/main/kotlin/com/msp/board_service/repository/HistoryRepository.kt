package com.msp.board_service.repository

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class HistoryRepository(private val template: ReactiveMongoTemplate) {

    companion object{
        const val COLLECTION_NM = "history"
    }

    fun insertBoardHistory(doc: Any): Mono<Any> {
        return template.insert(doc, COLLECTION_NM)
    }

    fun countHistoryBoard(query: Query) : Mono<Long>{
        return template.count(query, COLLECTION_NM)
    }
}