package com.marshio.redisdemo.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author masuo
 * @data 21/3/2022 上午11:13
 * @Description 消息发送, 这是一个服务，所以需要注入服务层
 */

@Service
@Slf4j
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;

    public RabbitMQSender() {
    }

    @Autowired
    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Object msg) {
        log.info("发送：{}", msg);
        //在没有交换机时，我们将消息直接发送到队列里，给出队列名称即可
        rabbitTemplate.convertAndSend("queue", "queue:" + msg);
        //当我们有交换机以后，我们可以将消息发给交换机，然后由交换机根据相应的模式发给队列，给出交换机名称
        rabbitTemplate.convertAndSend("fanoutExchange", "", "exchange:" + msg);
        //使用topic模式，
        rabbitTemplate.convertAndSend("*.topic1.*", "a.topic.b", "exchange:" + msg);
    }

    public void topicSend01(Object msg) {
        log.info("发送：{}", msg);
        rabbitTemplate.convertAndSend("topicExchange", "a.b.c.topic.d.f.e", msg + "1");//topicQueue1
        rabbitTemplate.convertAndSend("topicExchange", "a.c.topic.d.f.e", msg + "2");//topicQueue1
        rabbitTemplate.convertAndSend("topicExchange", "c.topic.e", msg + "3");//topicQueue1、topicQueue3
    }

    public void topicSend02(Object msg) {
        log.info("发送：{}", msg);
        rabbitTemplate.convertAndSend("topicExchange", "topic.d.f.e", msg + "1");//topicQueue1、topicQueue2
        rabbitTemplate.convertAndSend("topicExchange", "topic.d.f.e", msg + "2");//topicQueue1、topicQueue2
        rabbitTemplate.convertAndSend("topicExchange", "topic.e", msg + "3");//topicQueue1、topicQueue2、topicQueue4
    }

    public void topicSend03(Object msg) {
        log.info("发送：{}", msg);
        rabbitTemplate.convertAndSend("topicExchange", "c.topic.d", msg + "1");//topicQueue1、topicQueue3
    }

    public void topicSend04(Object msg) {
        log.info("发送：{}", msg);
        rabbitTemplate.convertAndSend("topicExchange", "topic.d", msg + "1");//topicQueue1、topicQueue2、topicQueue4
        rabbitTemplate.convertAndSend("topicExchange", "topic", msg + "2");//topicQueue1、topicQueue2
    }

    public void sendSecKillMsg(Object msg){
        log.info("skillExchange发送：{}",msg);
        rabbitTemplate.convertAndSend("skillExchange","secKill",msg);
    }
}
