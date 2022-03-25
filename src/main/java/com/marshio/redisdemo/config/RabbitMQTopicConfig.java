package com.marshio.redisdemo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author masuo
 * @data 21/3/2022 上午11:07
 * @Description rabbitmq配置类
 * rabbit MQ有多种模式
 * 1、简单模式：生产者将消息放入队列，消费者消费消息
 * 2、Fanout模式：广播模式，将消息通过交换机发送给与交换机绑定的所有队列
 * 3、Direct模式：
 * 4、Header模式：
 * 5、Topic模式：主题模式，使用通配符将消息通过交换机发送给
 */


@Configuration
public class RabbitMQTopicConfig {
    /**
     * 主题模式支持通配符
     * 1、* ：匹配路由键的一个词（ 》= 1）
     * 2、# ：匹配路由键的一个或多个词（ 》= 0）
     */

    //Topic模式队列
    private static final String QUEUE_TOPIC_01 = "topicQueue01";
    private static final String QUEUE_TOPIC_02 = "topicQueue02";
    private static final String QUEUE_TOPIC_03 = "topicQueue03";
    private static final String QUEUE_TOPIC_04 = "topicQueue04";

    //Topic模式交换机
    private static final String EXCHANGE_TOPIC = "topicExchange";

    //匹配模式
    private static final String PATTERN_TOPIC_01 = "#.topic.#";
    private static final String PATTERN_TOPIC_02 = "topic.#";
    private static final String PATTERN_TOPIC_03 = "*.topic.*";
    private static final String PATTERN_TOPIC_04 = "topic.*";

    @Bean
    public Queue topicQueue01() {
        return new Queue(QUEUE_TOPIC_01);
    }

    @Bean
    public Queue topicQueue02() {
        return new Queue(QUEUE_TOPIC_02);
    }

    @Bean
    public Queue topicQueue03() {
        return new Queue(QUEUE_TOPIC_03);
    }

    @Bean
    public Queue topicQueue04() {
        return new Queue(QUEUE_TOPIC_04);
    }

    // 交换机
    @Bean
    public TopicExchange topicExchange() {
        // 可以匹配: a.topic.b , 必须有 a 和 b
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    /*
     * 1、将队列绑定到交换器
     * 2、指定队列的匹配模式
     */
    @Bean
    public Binding topic01Bind() {
        return BindingBuilder.bind(topicQueue01()).to(topicExchange()).with(PATTERN_TOPIC_01);
    }

    @Bean
    public Binding topic02Bind() {
        return BindingBuilder.bind(topicQueue02()).to(topicExchange()).with(PATTERN_TOPIC_02);
    }

    @Bean
    public Binding topic03Bind() {
        return BindingBuilder.bind(topicQueue03()).to(topicExchange()).with(PATTERN_TOPIC_03);
    }

    @Bean
    public Binding topic04Bind() {
        return BindingBuilder.bind(topicQueue04()).to(topicExchange()).with(PATTERN_TOPIC_04);
    }


    // 秒杀相关

    private static final String SKILL_QUEUE = "skillQueue";
    private static final String SKILL_EXCHANGE = "skillExchange";

    @Bean
    public Queue secKillQueue() {
        return new Queue(SKILL_QUEUE);
    }

    @Bean
    public TopicExchange secKillExchange() {
        return new TopicExchange(SKILL_EXCHANGE);
    }

    @Bean
    public Binding bindSecKill() {
        return BindingBuilder.bind(secKillQueue()).to(secKillExchange()).with("secKill.#");
    }
}
