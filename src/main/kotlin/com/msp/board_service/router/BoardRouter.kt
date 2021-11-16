package com.msp.board_service.router

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.router
import java.net.URI

@Component
class BoardRouter {

    @Bean
    fun swaggerRouter() = router {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { permanentRedirect(URI("index.html")).build() }
        }
        resources("/**", ClassPathResource("/static"))
    }
}