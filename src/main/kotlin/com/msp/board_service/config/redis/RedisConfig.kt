package com.msp.board_service.config.redis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.serializer.StringRedisSerializer
import javax.inject.Inject

@Configuration
@EnableCaching
class RedisConfig {

    @Inject
    lateinit var redisProperties: RedisProperties

    @Autowired
    lateinit var env: Environment

//    @Value("{spring.redis.host}")
    var host: String = "localhost"

//    @Value("{spring.redis.port}")
    var port: Int = 6379


    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val conf = RedisStandaloneConfiguration(host,port)
        return LettuceConnectionFactory(conf)
//        return if(env.activeProfiles.contains("local")) {
//            // VM Option: -Dspring.profiles.active = local
//            val conf = RedisStandaloneConfiguration(redisProperties.host, 6379)
//            LettuceConnectionFactory(conf)
//        }else {
//            val clusterConfiguration = RedisClusterConfiguration(redisProperties.cluster.nodes)
//            val size = env.activeProfiles.size
//            println("### AWS ElastiCache Connection - clusterNodes: ${redisProperties.cluster.nodes}, " +
//                    "activeSpringProfile: ${if (size > 0) env.activeProfiles[0] else "unknown"}")
//            LettuceConnectionFactory(clusterConfiguration)
//        }
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String,Any>()
        redisTemplate.connectionFactory = redisConnectionFactory()
        redisTemplate.keySerializer = StringRedisSerializer()
        return redisTemplate
    }

}