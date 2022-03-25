package com.marshio.redisdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author masuo
 * @data 11/3/2022 上午9:15
 * @Description redis配置
 */

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //配置redisTemplate

        //配置redis的key的序列化器,我们设置了RedisTemplate的key为String类型，所以在序列化时我们使用String的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //配置redis的value的序列化器，我们设置RedisTemplate的value为Object类型，在redis中Object的序列化器为GenericJackson2JsonRedisSerializer，这也是redis的通用序列化器
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        //配置redis的hash类型的序列化,因为redis中hash算是比较特殊的类型，其key值又是一个key-value对象

        //hash key的序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //hash value的序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        //注入redis连接工厂
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }
}
