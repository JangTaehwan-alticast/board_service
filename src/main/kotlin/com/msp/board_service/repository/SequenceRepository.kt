package com.msp.board_service.repository

import com.msp.board_service.domain.Seq
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

@Repository
class SequenceRepository(private val template: ReactiveMongoTemplate) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    companion object{
        const val COLLECTION_NM = "seq"
    }

    fun getNextSeqIdUpdateInc(collectionName: String): Mono<Seq>{
        var query = Query(where("_id").`is`(collectionName))
        var update: Update = Update().inc("seq",1)
        var options: FindAndModifyOptions = FindAndModifyOptions().returnNew(true)
        return template.findAndModify<Seq>(query,update,options, COLLECTION_NM).switchIfEmpty{
            insertSeq(collectionName)
        }
    }

    fun insertSeq(collectionName: String): Mono<Seq>{
        return template.insert(Seq(collectionName,0), COLLECTION_NM)
    }

}