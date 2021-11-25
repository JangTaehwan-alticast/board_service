package com.msp.board_service.repository

import com.msp.board_service.domain.Board
import com.msp.board_service.domain.DeleteBoardHistory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class HistoryRepository(private val template: ReactiveMongoTemplate) {

    companion object{
        const val COLLECTION_NM = "history"
    }

    fun insertBoardHistory(dbh: DeleteBoardHistory): Mono<DeleteBoardHistory> {
        return template.insert(dbh, COLLECTION_NM)
    }
}