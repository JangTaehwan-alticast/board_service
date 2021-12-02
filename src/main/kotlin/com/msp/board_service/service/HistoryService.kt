package com.msp.board_service.service

import com.mongodb.client.result.DeleteResult
import com.msp.board_service.domain.Comment
import com.msp.board_service.domain.response.ModHistoryBoardResponse
import com.msp.board_service.domain.response.ModHistoryListResponse
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.CommentRepository
import com.msp.board_service.repository.HistoryRepository
import com.msp.board_service.util.CommonService
import com.msp.board_service.util.LogMessageMaker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat

interface HistoryServiceIn{
    fun getModHistoryBoardList(postId: String): Mono<Any>
    fun getModHistoryBoard(historyId: String): Mono<Any>
    fun restoreDeletedBoard(postId:String): Mono<DeleteResult>
}

@Service
class HistoryService: HistoryServiceIn {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var historyRepository: HistoryRepository

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var commentRepository: CommentRepository


    /**
     * 수정 목록 가져오기
     * @param postId 수정이력을 조회할 게시글의 아이디
     *
     * @suppress 1. history collection 에서 postId 와 PATCH 조건 검색
     * 2. time format 변환 후 응답
     */
    override fun getModHistoryBoardList(postId: String): Mono<Any> {
        val stopWatch = StopWatch()
        stopWatch.start()
        val query = Query(where("postId").`is`(postId).and("type").`is`("PATCH"))
        val historyList = ArrayList<ModHistoryListResponse>()
        return historyRepository.countHistoryBoard(query).flatMap { total->
            val resultMap = HashMap<String, Any>()
            resultMap["total"] = total
            if(total > 0L) {
                historyRepository.findBoardHistoryList(query).collectList().flatMap { modHistoryList ->
                    modHistoryList.forEach { modBoardHistory ->
                        var updatedDate =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(modBoardHistory.updatedDate!! * 1000)
                        historyList.add(
                            ModHistoryListResponse(
                                historyId = modBoardHistory.historyId,
                                postId = modBoardHistory.postId,
                                updatedDate = updatedDate,
                                version = modBoardHistory.version,
                                modifier = modBoardHistory.modifier
                            )
                        )
                    }
                    resultMap["data"] = historyList
                    Mono.just(resultMap)
                }
            }else{
                resultMap["data"] = historyList
                Mono.just(resultMap)
            }

        }
    }

    /**
     * 이전 버전의 게시글 조회(수정이력 조회)
     * @param historyId 수정된 게시글 조회를 위한 history 아이디
     *
     * @suppress 1. history Board 존재하는지 판단
     * 2. time format 변환 후 응답
     *
     * @exception CustomException.invalidHistoryId 유효하지 않은 historyId 입력시
     */
    override fun getModHistoryBoard(historyId: String): Mono<Any> {
        val stopWatch = StopWatch()
        stopWatch.start()
        val query = Query(where("historyId").`is`(historyId).and("type").`is`("PATCH"))
        return historyRepository.findExistHistoryBoard(query).flatMap {
            if(!it)
                return@flatMap Mono.error(CustomException.invalidHistoryId(historyId))
            historyRepository.findModBoardHistory(query)
        }.flatMap { modBoardHistory ->
            var updatedDate = CommonService.epochToString(modBoardHistory.updatedDate!!)
            var modHistoryBoardResponse = ModHistoryBoardResponse(
                historyId = modBoardHistory.historyId,
                postId = modBoardHistory.postId,
                type = modBoardHistory.type,
                board = modBoardHistory.board,
                modifier = modBoardHistory.modifier,
                updatedDate = updatedDate,
                version = modBoardHistory.version
            )
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "HistoryService", "getModHistoryBoard")
            logger.info(logMsg)
            Mono.just(modHistoryBoardResponse)
        }
    }

    /**
     * 삭제된 게시글 복원
     * @param postId 삭제 게시글의 아이디
     *
     * @suppress 1. history Board 존재하는지 판단
     * 2. history 에서 게시글과 댓글을 collect
     * 3. board collection 에 게시글 document 삽입
     * 4. comment 존재하는 경우 comment collection 댓글 document 삽입
     *
     * @exception CustomException.invalidPostId 유효하지 않은 postId 입력시
     */
    override fun restoreDeletedBoard(postId: String): Mono<DeleteResult> {
        val stopWatch = StopWatch()
        stopWatch.start()
        val query = Query(where("postId").`is`(postId).and("type").`is`("DELETE"))
        var commentList = ArrayList<Comment>()
        return historyRepository.findExistHistoryBoard(query).flatMap {
            if (!it)
                return@flatMap Mono.error(CustomException.invalidPostId(postId))
            historyRepository.findDeleteBoardHistory(query)
        }.flatMap { deleteBoardHistory ->
            commentList = if (!deleteBoardHistory.comment.isNullOrEmpty()) {
                deleteBoardHistory.comment!!
            } else {
                ArrayList()
            }
            deleteBoardHistory.board!!.useYn = "11"
            boardRepository.insertBoard(deleteBoardHistory.board!!)
        }.flatMap {
            commentRepository.restoreComment(commentList).collectList()
        }.flatMap {
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "CommentService", "restoreDeletedBoard")
            logger.info(logMsg)
            historyRepository.deleteBoardHistory(query)
        }
    }
}