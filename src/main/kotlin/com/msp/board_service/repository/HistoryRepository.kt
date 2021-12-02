package com.msp.board_service.repository

import com.mongodb.client.result.DeleteResult
import com.msp.board_service.domain.DeleteBoardHistory
import com.msp.board_service.domain.ModifyBoardHistory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class HistoryRepository(private val template: ReactiveMongoTemplate) {

    companion object{
        const val COLLECTION_NM = "history"
    }

    fun findBoardHistoryList(query: Query): Flux<ModifyBoardHistory> {
        return template.find(query,ModifyBoardHistory::class.java, COLLECTION_NM)
    }

    fun findModBoardHistory(query: Query): Mono<ModifyBoardHistory> {
        return template.findOne(query,ModifyBoardHistory::class.java, COLLECTION_NM)
    }

    fun findDeleteBoardHistory(query: Query): Mono<DeleteBoardHistory> {
        return template.findOne(query,DeleteBoardHistory::class.java, COLLECTION_NM)
    }

    fun deleteBoardHistory(query: Query): Mono<DeleteResult> {
        return template.remove(query, COLLECTION_NM)
    }

    fun insertBoardHistory(doc: Any): Mono<Any> {
        return template.insert(doc, COLLECTION_NM)
    }

    fun countHistoryBoard(query: Query) : Mono<Long>{
        return template.count(query, COLLECTION_NM)
    }

    fun findExistHistoryBoard(query: Query) : Mono<Boolean> {
        return template.exists(query, COLLECTION_NM)
    }
}