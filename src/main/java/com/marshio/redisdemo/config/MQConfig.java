package com.marshio.redisdemo.config;

import com.rabbitmq.client.AMQP;
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
public class MQConfig {

    //普通模式的队列名
    private static final String QUEUE = "queue";
    //广播模式的队列名
    private static final String QUEUE_FANOUT_01 = "queue_fanout_01";
    private static final String QUEUE_FANOUT_02 = "queue_fanout_02";
    // 广播模式的交换机名
    private static final String EXCHANGE_FANOUT = "fanoutExchange";

    // 一般queue
    @Bean
    public Queue queue() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return new Queue(QUEUE, true);
    }

    /* 广播模式 START */
    // 广播模式的队列
    @Bean
    public Queue queueFanout01() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return new Queue(QUEUE_FANOUT_01);
    }

    @Bean
    public Queue queueFanout02() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return new Queue(QUEUE_FANOUT_02);
    }

    // 广播模式的交换机
    @Bean
    public FanoutExchange fanoutExchange() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return new FanoutExchange(EXCHANGE_FANOUT);
    }

    // 绑定队列到交换机上
    @Bean
    public Binding fanout01Bind() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return BindingBuilder.bind(queueFanout01()).to(fanoutExchange());
    }

    @Bean
    public Binding fanout02Bind() {
        // 两个参数，第一个是队列名，第二个是队列是否需要持久化
        return BindingBuilder.bind(queueFanout02()).to(fanoutExchange());
    }

    /* 广播模式 END */
}
