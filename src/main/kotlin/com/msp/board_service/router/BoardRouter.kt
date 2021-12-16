package com.msp.board_service.router

import com.msp.board_service.handler.BoardHandler
import com.msp.board_service.handler.CommentHandler
import com.msp.board_service.handler.HistoryHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.router
import java.net.URI

@Configuration
class BoardRouter(val boardHandler: BoardHandler,val commentHandler: CommentHandler, val historyHandler: HistoryHandler) {

    @Bean("BoardHandlerV1.0")
    fun boardRouter() = router {
        accept(TEXT_HTML).nest {
            GET("/") { permanentRedirect(URI("index.html")).build() }
        }
        accept(APPLICATION_JSON).nest{
            "/board-service/v1".nest {
                GET("/board",boardHandler::getBoardList)                                    //게시글 리스트 조회(검색포함)
                POST("/board",boardHandler::insertBoard)                                    //게시글 입력
                GET("/board/{postId}",boardHandler::getOneBoard)                            //게시글 단건조회
                PATCH("/board/{postId}",boardHandler::modifyBoard)                          //게시글 수정
                DELETE("/board/{postId}",boardHandler::deleteBoard)                         //게시글 삭제

                GET("/board/{postId}/comment",commentHandler::findCmntList)                 //댓글 리스트 조회
                POST("/board/{postId}/comment",commentHandler::insertCmnt)                  //댓글 입력
                PATCH("/board/{commentId}/comment",commentHandler::modifyCmnt)              //댓글 수정
                DELETE("/board/{commentId}/comment",commentHandler::deleteCmnt)             //댓글 삭제

                GET("/board/{postId}/history",historyHandler::getModHistoryBoardList)       //게시글 수정이력 조회
                GET("/board/{historyId}/history-one",historyHandler::getModHistoryBoard)    //게시글 버전별 조회
                PATCH("/board/{postId}/restoration",historyHandler::restoreDeletedBoard)    //삭제된 게시글 복원
            }
        }
        resources("/**", ClassPathResource("static/"))
    }

}