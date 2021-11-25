package com.msp.board_service.service

import com.mongodb.client.result.DeleteResult
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.DeleteBoardHistory
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.HistoryRepository
import com.msp.board_service.repository.SequenceRepository
import com.msp.board_service.util.CommonService
import com.msp.board_service.util.LogMessageMaker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import org.springframework.util.StringUtils
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

    // TODO: 2021/11/24 function 별 소요시간 체크, log 찍기
    fun insertBoard(board: Board): Mono<Board> {
        val stopWatch = StopWatch()
        stopWatch.start()
        //다국어 제목 누락시
        if(board.title.isNullOrEmpty()){
            throw CustomException.invalidParameter("title")
        }else{//제목 50자 초과시
            board.title!!.stream().forEach {
                if(it.value.length>50){
                    throw CustomException.exceedMaxValue("title",it.value.substring(0,15)+"...",50)
                }
            }
        }
        if(!StringUtils.hasText(board.nickName)){//닉네임 누락시
            throw CustomException.invalidParameter("nickName")
        }else if(board.nickName!!.length > 20){//닉네임 길이 초과시
            throw CustomException.exceedMaxValue("nickName",board.nickName!!,20)
        }
        if(!StringUtils.hasText(board.contents)){//내용 누락시
            throw CustomException.invalidParameter("contents")
        }else if(board.contents!!.length>255){//내용 길이 초과시
            throw CustomException.exceedMaxValue("contents",board.contents!!.substring(15)+"...",255)
        }
        return seqRepository.getNextSeqIdUpdateInc("boards").flatMap {
            if(!StringUtils.hasText(board.category)) board.category = "all"
            if(board.exposureDate == null){
                board.useYn = "11"
            }else{
                board.exposureDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(board.exposureDate!! *1000)
                board.useYn = "01"
            }
            board.createdDate = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
            board.createdDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(board.createdDate!! *1000)
            board.postId = "post_"+it.seq.toString()
            var msg = LogMessageMaker.getFunctionLog(stopWatch,"BoardService","insertBoard")
            logger.info(msg)
            boardRepository.insertBoard(board)
        }

    }

    fun deleteBoard(postId:String):Mono<DeleteResult>{
        val stopWatch = StopWatch()
        stopWatch.start()
        if(!StringUtils.hasText(postId)){
            throw CustomException.invalidParameter("postId")
        }
        val dbh = DeleteBoardHistory()
        return getOneBoard(postId).flatMap {
            dbh.postId = it.postId
            dbh.board = it
            dbh.deletedDate = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()
            dbh.deletedDateRes = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dbh.deletedDate!! *1000)
            seqRepository.getNextSeqIdUpdateInc("history")
        }.flatMap {
            dbh.historyId = "history_${it.seq}"
            historyRepository.insertBoardHistory(dbh)
        }.flatMap {
            val logMsg = LogMessageMaker.getFunctionLog(stopWatch, "BoardService", "deleteBoard")
            val query = Query(Criteria.where("postId").`is`(postId))
            logger.info(logMsg)
            boardRepository.deleteBoard(query)
        }
    }
    fun getOneBoard(postId:String):Mono<Board>{
        val query = Query(Criteria.where("postId").`is`(postId))
        return boardRepository.findOneBoard(query)
    }




}