package com.msp.board_service.service

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.msp.board_service.domain.Comment
import com.msp.board_service.domain.request.InsertCommentRequest
import com.msp.board_service.domain.request.ModCommentRequest
import com.msp.board_service.domain.response.CommentResponse
import com.msp.board_service.domain.response.InsertCommentResponse
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.CommentRepository
import com.msp.board_service.repository.SequenceRepository
import com.msp.board_service.util.CommonService
import com.msp.board_service.util.LogMessageMaker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono

interface CommentServiceIn {
    fun insertCmmt(postId: String, InsertCmntDTO:InsertCommentRequest): Mono<Any>
    fun findCmntList(postId: String, size: Long, page: Long): Mono<Any>
    fun deleteCmnt(commentId: String): Mono<DeleteResult>
    fun modifyCmnt(commentId: String, ModCmntDTO:ModCommentRequest): Mono<UpdateResult>
}

@Service
class CommentService : CommentServiceIn {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var cmntRepository: CommentRepository

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var seqRepository: SequenceRepository

    /**
     * 댓글 입력
     * @param postId 댓글 대상 게시글의 아이디
     * @param insertCmntDTO 댓글입력용 DTO
     * @suppress 1. 필수 파라미터 유효성 검증
     * 2. 대상 게시글 존재하는지 판단
     * 3. comment seq 생성 및 commentId 부여
     * 4. comment collection 댓글 document 삽입
     *
     * @exception CustomException.invalidParameter nickName,contents 누락시
     * @exception CustomException.exceedMaxValue 필드의 maxValue 초과시
     * @exception CustomException.invalidPostId 유효하지 않은 postId 입력시
     */
    override fun insertCmmt(postId: String, insertCmntDTO: InsertCommentRequest): Mono<Any> {
        val stopWatch = StopWatch()
        stopWatch.start()

        var commentId = ""
        var query = Query.query(Criteria.where("postId").`is`(postId))

        return boardRepository.findExistBoard(query).flatMap {
            if(!it)
                return@flatMap Mono.error(CustomException.invalidPostId(postId))
            if(insertCmntDTO.nickName.isNullOrEmpty()){
                return@flatMap Mono.error(CustomException.invalidParameter("nickName"))
            }else if(insertCmntDTO.nickName!!.length > 20){
                return@flatMap Mono.error(CustomException.exceedMaxValue("nickName",insertCmntDTO.nickName!!.substring(15),20))
            }else if(insertCmntDTO.contents.isNullOrEmpty()){
                return@flatMap Mono.error(CustomException.invalidParameter("contents"))
            }else if(insertCmntDTO.contents!!.length > 255){
                return@flatMap Mono.error(CustomException.exceedMaxValue("contents",insertCmntDTO.contents!!.substring(15),255))
            }
            seqRepository.getNextSeqIdUpdateInc("comment")
        }.flatMap { seq ->
            commentId = "comment_${seq.seq}"
            Mono.just(commentId)
            val createdDate = CommonService.getNowEpochTime()
            var comment = Comment(
                postId = postId,
                commentId = commentId,
                nickName = insertCmntDTO.nickName,
                contents = insertCmntDTO.contents,
                createdDate = createdDate,
                lastUpdatedDate = createdDate
            )
            cmntRepository.insertComment(comment)
        }.flatMap { Comment ->
            val createdDateRes = CommonService.epochToString(Comment.createdDate!!)
            var resCmnt = InsertCommentResponse(
                postId = postId,
                commentId = commentId,
                nickName = insertCmntDTO.nickName,
                contents = insertCmntDTO.contents,
                createdDate = createdDateRes,
            )
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch,"CommentService","insertComment")
            logger.debug(logMsg)
            Mono.just(resCmnt)
        }
    }

    /**
     * 댓글 리스트 조회
     *
     * @param postId 조회 대상의 게시글 아이디
     * @param size interval
     * @param page offset
     *
     * @suppress 1. comment 조회 project 생성
     * 2. 조회 대상 게시글의 postId Criteria 추가
     * 3. 댓글 count 및 반환
     */
    override fun findCmntList(postId: String, size: Long, page: Long): Mono<Any> {
        val stopWatch = StopWatch()
        stopWatch.start()

        val countAggOps = ArrayList<AggregationOperation>()
        val listAggOps = ArrayList<AggregationOperation>()

        //select
        val project = Aggregation.project(
            "postId","commentId","nickName","contents","createdDate","lastUpdatedDate"
        )
        listAggOps.add(project)
        //where
        val criteria = Criteria.where("postId").`is`(postId)
        val match = Aggregation.match(criteria)
        listAggOps.add(match)
        countAggOps.add(match)

        // paging
        val limitValue = if (size > 0L) {
            size
        } else {
            10L
        }
        val skipValue = if (page > 0L) {
            (page - 1) * limitValue
        } else {
            0L
        }
        val skip: AggregationOperation = Aggregation.skip(skipValue)
        val limit: AggregationOperation = Aggregation.limit(limitValue)
        listAggOps.add(skip)
        listAggOps.add(limit)

        val countAgg = Aggregation.newAggregation(countAggOps)
        val listAgg = Aggregation.newAggregation(listAggOps)

        val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "BoardService", "getBoardList")
        return cmntRepository.findCommentCount(countAgg).flatMap { total ->
            val resultMap = HashMap<String, Any>()
            resultMap["page"] = page
            resultMap["size"] = limitValue
            resultMap["total"] = total
            val commentList = ArrayList<CommentResponse>()
            cmntRepository.findCommentList(listAgg).collectList().flatMap { oriComment ->
                oriComment.forEach { comment ->
                    val createdDate  = CommonService.epochToString(comment.createdDate!!)
                    val updatedDate  = CommonService.epochToString(comment.lastUpdatedDate!!)
                    commentList.add(
                        CommentResponse(
                            postId = comment.postId,
                            commentId = comment.commentId,
                            nickName = comment.nickName,
                            contents = comment.contents,
                            createdDate = createdDate,
                            lastUpdatedDate = updatedDate
                        )
                    )
                }
                resultMap["data"] = commentList
                logger.debug(logMsg)
                Mono.just(resultMap)
            }
        }
    }

    /**
     * 댓글 삭제
     * @param commentId 삭제 대상 댓글의 아이디
     *
     * @suppress 1. 해당 댓글이 존재하는지 판단
     * 2. 댓글 삭제 (history 별도 저장 없음)
     *
     * @exception CustomException.invalidCommentId 유효하지 않은 commentId 입력시
     */
    override fun deleteCmnt(commentId: String): Mono<DeleteResult> {
        val stopWatch = StopWatch()
        stopWatch.start()
        val query = Query(Criteria.where("commentId").`is`(commentId))
        return cmntRepository.findExistComment(query).flatMap {
            if(!it)
                return@flatMap Mono.error(CustomException.invalidCommentId(commentId))
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch,"CommentService","deleteCmnt")
            logger.debug(logMsg)
            cmntRepository.deleteComment(query)
        }
    }

    /**
     * 댓글 수정
     * @param commentId 수정 대상댓글의 아이디
     * @param modCmntDTO 수정 필드 DTO
     *
     * @suppress 1. 대상 댓글 존재하는지 판단
     * 2. 수정 필드 유효성 검증
     * 3. 업데이트 날짜 추가
     * 4. 원본 댓글 수정
     *
     * @exception CustomException.invalidCommentId 유효하지 않은 commentId 입력시
     * @exception CustomException.invalidParameter 필수 파라미터 누락시
     * @exception CustomException.exceedMaxValue 필드 최대값 초과시
     */
    override fun modifyCmnt(commentId: String, modCmntDTO: ModCommentRequest): Mono<UpdateResult> {
        val stopWatch = StopWatch()
        stopWatch.start()
        var query = Query(Criteria.where("commentId").`is`(commentId))
        return cmntRepository.findExistComment(query).flatMap {
            if(!it)
                return@flatMap Mono.error(CustomException.invalidCommentId(commentId))
            if(modCmntDTO.contents.isNullOrEmpty())
                return@flatMap Mono.error(
                    CustomException.invalidParameter("contents")
                )
            else if(modCmntDTO.contents!!.length > 255)
                return@flatMap Mono.error(
                    CustomException.exceedMaxValue("contents",modCmntDTO.contents!!.substring(0,15)+"...",255)
                )
            var updatedDate = CommonService.getNowEpochTime()
            var update = Update()
            update.set("contents",modCmntDTO.contents)
            update.set("lastUpdatedDate",updatedDate)
            val logMsg= LogMessageMaker.getFunctionLog(stopWatch,"commentService","modifyBoard")
            logger.debug(logMsg)
            cmntRepository.modifyComment(query,update)
        }
    }
}