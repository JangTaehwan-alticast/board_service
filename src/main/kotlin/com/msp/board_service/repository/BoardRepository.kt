package com.msp.board_service.repository

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.msp.board_service.domain.Board
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class BoardRepository(private val template: ReactiveMongoTemplate) {


    companion object{
        const val COLLECTION_NM = "boards"
    }

    fun findOneBoard(){

    }

    fun findBoardList(agg:Aggregation):Flux<Board>{
        return template.aggregate(agg, COLLECTION_NM,Board::class.java)
    }

    fun findBoardCount(agg:Aggregation):Mono<Long>{
        return template.aggregate(agg, COLLECTION_NM,Any::class.java).count()
    }

    fun insertBoard(board: Board) : Mono<Board> {
        return template.insert(board, COLLECTION_NM)
    }

    fun deleteBoard(query: Query) : Mono<DeleteResult> {
        return template.remove(query, COLLECTION_NM)
    }

    //일반 update 와 updateFirst 의 차이점이 뭔가
    fun modifyBoard(query: Query, update: Update) : Mono<UpdateResult> {
        return template.updateFirst(query,update, COLLECTION_NM)
    }

    fun findOneBoardForModify(query:Query) : Mono<Board> {
        return template.findOne(query,Board::class.java)
    }



}