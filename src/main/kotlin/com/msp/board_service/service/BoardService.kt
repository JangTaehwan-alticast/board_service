package com.msp.board_service.service

import com.msp.board_service.domain.BoardTest
import com.msp.board_service.repository.BoardRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class BoardService {

    @Autowired
    lateinit var boardRepository: BoardRepository

    fun insertBoardTest(board: BoardTest): Mono<BoardTest> {
        return boardRepository.insertBoard(board)
    }
}