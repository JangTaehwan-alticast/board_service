package com.msp.board_service.service

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate

@SpringBootTest
class SimpleTest {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String,Any>




}