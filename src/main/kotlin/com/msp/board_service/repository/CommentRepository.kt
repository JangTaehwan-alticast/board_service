package com.msp.board_service.repository

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.msp.board_service.domain.Comment
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class CommentRepository(private val template: ReactiveMongoTemplate) {

    companion object{
        const val COLLECTION_NM = "comment"
    }

    fun findCommentList(agg: Aggregation):Flux<Comment>{
        return template.aggregate(agg, COLLECTION_NM, Comment::class.java)
    }

    fun findCommentCount(agg:Aggregation):Mono<Long>{
        return template.aggregate(agg, COLLECTION_NM,Any::class.java).count()
    }

    fun insertComment(comment:Comment): Mono<Comment>{
        return template.insert(comment, COLLECTION_NM)
    }

    fun restoreComment(commentList: ArrayList<Comment>): Flux<Comment> {
        return template.insert(commentList, COLLECTION_NM)
    }

    fun modifyComment(query: Query, update: Update) : Mono<UpdateResult> {
        return template.updateFirst(query,update, COLLECTION_NM)
    }

    fun deleteComment(query: Query) : Mono<DeleteResult> {
        return template.remove(query, COLLECTION_NM)
    }

    fun findAllComment(query: Query) : Flux<Comment> {
        return template.find(query, Comment::class.java, COLLECTION_NM)
    }

    fun findExistComment(query: Query) : Mono<Boolean> {
        return template.exists(query, COLLECTION_NM)
    }


}