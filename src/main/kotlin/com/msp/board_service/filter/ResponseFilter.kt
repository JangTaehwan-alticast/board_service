package com.msp.board_service.filter

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class ResponseFilter : WebFilter{

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        /**
         * 응답 헤더에 content-type 추가
         */
            exchange
                .response
                .headers
                .add("content-type","application/json; charset=UTF-8")

        return chain.filter(exchange)
    }
}