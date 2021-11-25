package com.msp.board_service.router

import com.msp.board_service.handler.BoardHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.router
import java.net.URI

@Configuration
class BoardRouter(val boardHandler: BoardHandler) {


    @Bean("BoardHandlerV1.0")
    fun boardRouter() = router {
        accept(TEXT_HTML).nest {
            GET("/") { permanentRedirect(URI("index.html")).build() }
            GET("/board",boardHandler::getBoardList)
        }
        accept(APPLICATION_JSON).nest{
            "/board-service/v1".nest {
                GET("/board",boardHandler::getBoardList)
                POST("/board",boardHandler::insertBoard)
//
//                GET("/board/{postId}")
//                PATCH("/board/{postId")
                DELETE("/board/{postId}",boardHandler::deleteBoard)
//
//                GET("/board/history/{historyId}")
//
//                GET("/board/comment/{postId}")
//                POST("/board/comment/{postId}")
//                PATCH("/board/comment/{postId}")
//                DELETE("/board/comment/{postId}")
//
//                POST("/board/restore/{postId}")


            }
        }
        resources("/**", ClassPathResource("static/"))
    }

}