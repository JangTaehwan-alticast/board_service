package com.msp.board_service.service

import com.msp.board_service.aop.LogExecute
import com.msp.board_service.aop.LoggerLevel
import com.msp.board_service.domain.Board
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.SequenceRepository
import com.msp.board_service.util.CommonService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class BoardService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Autowired
    lateinit var seqRepository: SequenceRepository

    @Autowired
    lateinit var commonService: CommonService

//    @LogExecute(level = LoggerLevel.INFO, message = "메소드 호출")
    fun insertBoard(board: Board): Mono<Board> {
        //다국어 제목 누락시
        if(board.title.isNullOrEmpty()){
            throw CustomException.invalidParameter("title")
        }else{//제목 50자 초과시
            board.title!!.stream().forEach {
                if(it.value.length>50){
                    throw CustomException.exceedMaxValue("title",it.value.substring(0,20)+"...",50)
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
            throw CustomException.exceedMaxValue("contents",board.contents!!.substring(0,20)+"...",255)
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
            boardRepository.insertBoard(board)
        }

    }
}