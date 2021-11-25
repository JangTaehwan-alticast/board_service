package com.msp.board_service.service

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.DeleteBoardHistory
import com.msp.board_service.domain.ModifyBoardDTO
import com.msp.board_service.domain.ModifyBoardHistory
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.HistoryRepository
import com.msp.board_service.repository.SequenceRepository
import com.msp.board_service.util.CommonService
import com.msp.board_service.util.LogMessageMaker
import com.msp.board_service.util.MakeWhereCriteria
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class BoardService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var seqRepository: SequenceRepository

    @Autowired
    lateinit var historyRepository: HistoryRepository

    @Autowired
    lateinit var commonService: CommonService

    /**
     * 게시글 검색 포함 목록
     */
    fun getBoardList(
            postId: String, category: String, nickName: String, title: String,
            contents: String, q: String, page: Long, size: Long
    ):Mono<Any>{
        val stopWatch = StopWatch()
        stopWatch.start()
        val totalCountAggOps = ArrayList<AggregationOperation>()//검색 결과 total count Agg
        val listAggOps = ArrayList<AggregationOperation>()//list 검색용 Agg

        //select
        val project = Aggregation.project("postId","category", "nickName", "title", "contents", "createdDateRes")

        //where
        val criteria: Criteria = setMatchCriteria(
            postId, category, nickName, title, contents, q
        )
        val match: AggregationOperation = Aggregation.match(criteria)

        listAggOps.add(project)
        listAggOps.add(match)
        totalCountAggOps.add(match)

        // TODO: 2021/11/25 order by 추가 예정


        //paging
        val limitValue = if(size > 0L){
            size
        }else{
            10L
        }
        val skipValue = if(page > 0L){
            (page-1) * limitValue
        }else{
            0L
        }
        val limit = Aggregation.limit(limitValue)
        val skip = Aggregation.skip(skipValue)

        listAggOps.add(limit)
        listAggOps.add(skip)

        val listAgg = Aggregation.newAggregation(listAggOps)
        val totalAgg = Aggregation.newAggregation(totalCountAggOps)

        val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "BoardService", "getBoardList")
        logger.info(logMsg) //여기에 query 붙일 수 있음 좋겠군
        logger.error("query = ${listAgg}")
        return boardRepository.findBoardCount(totalAgg).flatMap {
            val resultMap = HashMap<String, Any>()
            resultMap.put("total",it)
            if(it > 0L){
                boardRepository.findBoardList(listAgg).collectList().flatMap {
                    resultMap["data"] = it
                    Mono.just(resultMap)
                }
            }else{
                resultMap["data"] = ArrayList<Board>()
                Mono.just(resultMap)
            }
        }
    }
     fun setMatchCriteria(
            postId: String, category: String, nickName: String,
            title: String, contents: String, q: String
     ): Criteria {
         var criteria = Criteria()
         var andCriteria = ArrayList<Criteria>()

         if(!postId.isNullOrEmpty()){
             var paramValue = postId.split("?")
             if(paramValue.size == 2){
                 andCriteria.add(MakeWhereCriteria.makeWhereCriteria("postId",paramValue[0],paramValue[1]))
             }
         }
         if(!category.isNullOrEmpty()){
             var paramValue = postId.split("?")
             if(paramValue.size == 2){
                 andCriteria.add(MakeWhereCriteria.makeWhereCriteria("category",paramValue[0],paramValue[1]))
             }
         }
         if(!nickName.isNullOrEmpty()){
             var paramValue = postId.split("?")
             if(paramValue.size == 2){
                 andCriteria.add(MakeWhereCriteria.makeWhereCriteria("nickName",paramValue[0],paramValue[1]))
             }
         }
         //title은 배열의 형태인데 검색을 Criteria 어떻게 지정해야할 지 고만해볼 것.
         if(!title.isNullOrEmpty()){
             var paramValue = postId.split("?")
             if(paramValue.size == 2){
                 andCriteria.add(MakeWhereCriteria.makeWhereCriteria("title",paramValue[0],paramValue[1]))
             }
         }
         if(!contents.isNullOrEmpty()){
             var paramValue = postId.split("?")
             if(paramValue.size == 2){
                 andCriteria.add(MakeWhereCriteria.makeWhereCriteria("contents",paramValue[0],paramValue[1]))
             }
         }
         if(!q.isNullOrEmpty()){
             val qList = StringUtils.deleteWhitespace(q).split(",")
             var paramList = ArrayList<String>()
             var idx = -1
             qList.forEach{
                 if(it.indexOf("%")>0){
                     paramList.add(it)
                     idx++
                 }else{
                     paramList[idx] = paramList[idx]+","+it
                 }
             }

             paramList.forEach {
                 var param = it.split("%")
                 if(param.size == 2){
                     var paramName = param[0]
                     var paramValues = param[1].split("?")
                     if(paramValues.size == 2){
                         var valueType = "string"
                         if(StringUtils.equalsAnyIgnoreCase(paramName,
                             "exposureDate","createdDate","lastUpdateDate")){
                             valueType = "Long"
                         }else if(StringUtils.equalsAnyIgnoreCase(paramName,
                             "input any Int value")){
                             valueType = "int"
                         }
                         andCriteria
                             .add(MakeWhereCriteria.makeWhereCriteria(paramName, paramValues[0],paramValues[1],valueType))
                     }
                 }
             }
         }
         // And Criteria 가 존재하는 경우 Criteria And Operator 추가
         if(andCriteria.size>0){
             criteria.andOperator(*andCriteria.toTypedArray())
         }
         return criteria
    }

    /**
     * 게시글 입력
     */
    fun insertBoard(board: Board): Mono<Board> {
        val stopWatch = StopWatch()
        stopWatch.start()
        //다국어 제목 누락시
        if(board.title.isNullOrEmpty()){
            throw CustomException.invalidParameter("title")
        }else{//제목 50자 초과시
            board.title!!.stream().forEach {
                if(it.value.length>50){
                    throw CustomException
                        .exceedMaxValue("title",it.value.substring(0,15)+"...",50)
                }
            }
        }
        if(!board.nickName.isNullOrEmpty()){//닉네임 누락시
            throw CustomException.invalidParameter("nickName")
        }else if(board.nickName!!.length > 20){//닉네임 길이 초과시
            throw CustomException.exceedMaxValue("nickName",board.nickName!!,20)
        }
        if(!board.contents.isNullOrEmpty()){//내용 누락시
            throw CustomException.invalidParameter("contents")
        }else if(board.contents!!.length>255){//내용 길이 초과시
            throw CustomException.exceedMaxValue("contents",board.contents!!.substring(15)+"...",255)
        }
        return seqRepository.getNextSeqIdUpdateInc("boards").flatMap {
            if(!board.category.isNullOrEmpty()){
                board.category = "all"
            }
            if(board.exposureDate != null){
                board.exposureDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(board.exposureDate!! *1000)
            }
            board.useYn = "11"
            board.createdDate = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
            board.createdDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(board.createdDate!! *1000)
            board.postId = "post_${it.seq}"
            var msg = LogMessageMaker.getFunctionLog(stopWatch,"BoardService","insertBoard")
            logger.info(msg)
            boardRepository.insertBoard(board)
        }

    }

    /**
     * 게시글 삭제
     */
    fun deleteBoard(postId:String):Mono<DeleteResult>{
        val stopWatch = StopWatch()
        stopWatch.start()
        if(!postId.isNullOrEmpty()){
            throw CustomException.invalidParameter("postId")
        }
        val deleteBoardHistory = DeleteBoardHistory()
        return getOneBoard(postId).flatMap {
            it.useYn = "00"
            deleteBoardHistory.type = "DELETE"
            deleteBoardHistory.postId = it.postId
            deleteBoardHistory.board = it
            deleteBoardHistory.deletedDate = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
            deleteBoardHistory.deletedDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(deleteBoardHistory.deletedDate!! *1000)
            seqRepository.getNextSeqIdUpdateInc("history")
        }.flatMap {
            deleteBoardHistory.historyId = "history_${it.seq}"
            historyRepository.insertBoardHistory(deleteBoardHistory)
        }.flatMap {
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "BoardService", "deleteBoard")
            val query = Query(where("postId").`is`(postId))
            logger.info(logMsg)
            boardRepository.deleteBoard(query)
        }
    }

    /**
     * 게시글 수정
     */
    fun updateBoard(postId:String, modifyBoardDTO:ModifyBoardDTO):Mono<UpdateResult>{
        var stopWatch = StopWatch()
        stopWatch.start()
        if(!postId.isNullOrEmpty()) throw CustomException.invalidParameter("postId")
        if(!modifyBoardDTO.modifier.isNullOrEmpty()) throw CustomException.invalidParameter("modifier")

        var modifyBoardHistory = ModifyBoardHistory()
        return seqRepository.getNextSeqIdUpdateInc("history").flatMap {
            modifyBoardHistory.historyId = "history_${it.seq}"
            getCountHistoryBoard(postId,"PATCH")
        }.flatMap {
            var nowVer = it+1
            modifyBoardHistory.version = "V${nowVer}.0"
            getOneBoard(postId)
        }.flatMap {
            modifyBoardHistory.board = it
            modifyBoardHistory.postId = it.postId
            modifyBoardHistory.type = "PATCH"
            modifyBoardHistory.updateDate = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
            modifyBoardHistory.updateDateRes =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(modifyBoardHistory.updateDate!! *1000)
            modifyBoardHistory.modifier = modifyBoardDTO.modifier
            historyRepository.insertBoardHistory(modifyBoardHistory)
        }.flatMap {
            var query = Query(where("postId").`is`(postId))
            var update = Update()
            modifyBoardDTO.title?.let { update.set("title",modifyBoardDTO.title) }
            modifyBoardDTO.contents?.let { update.set("contents",modifyBoardDTO.contents) }
            modifyBoardDTO.modifier?.let { update.set("modifier",modifyBoardDTO.modifier) }
            update.set("lastUpdateDate",modifyBoardHistory.updateDate)
            update.set("lastUpdateDateRes",modifyBoardHistory.updateDateRes)
            LogMessageMaker.getFunctionLog(stopWatch,"BoardService","modifyBoard")
            boardRepository.modifyBoard(query,update)
        }
    }

    /**
     * 게시판 글 단건(삭제,수정용)
     */
    fun getOneBoard(postId:String):Mono<Board>{
        val query = Query(where("postId").`is`(postId))
        return boardRepository.findOneBoardForModify(query)
    }

    /**
     * history type 별 count 조회
     */
    fun getCountHistoryBoard(postId:String,type:String):Mono<Long>{
        val query = Query(where("postId").`is`(postId).and("type").`is`(type))
        return historyRepository.countHistoryBoard(query)
    }





}