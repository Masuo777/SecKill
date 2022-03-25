package com.marshio.redisdemo.mq;

import com.marshio.redisdemo.pojo.Order;
import com.marshio.redisdemo.pojo.SecKillMessage;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.GoodsService;
import com.marshio.redisdemo.service.OrderService;
import com.marshio.redisdemo.utils.JsonUtil;
import com.marshio.redisdemo.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author masuo
 * @data 21/3/2022 上午11:18
 * @Description 消息接收者
 */

@Service
@Slf4j
public class RabbitMQReceiver {
    private OrderService orderService;
    private GoodsService goodsService;
    private RedisTemplate<String, Object> redisTemplate;

    public RabbitMQReceiver() {
    }

    @Autowired
    public RabbitMQReceiver(GoodsService goodsService, RedisTemplate<String, Object> redisTemplate, OrderService orderService) {
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
    }

    // 发送时要给出队列名称/交换机名称，接收时，给出队列名称即可，因为我们是通过队列接收消息，交换机在内部完成广播，
    //队列直接接受
    @RabbitListener(queues = "queue")
    public void queueReceive(Object msg) {
        log.info("接收：{}", msg);
    }

    //接收
    @RabbitListener(queues = "queue_fanout_01")
    public void exchangeReceive01(Object msg) {
        log.info("queue_fanout_01接收：{}", msg);
    }

    @RabbitListener(queues = "queue_fanout_02")
    public void exchangeReceive02(Object msg) {
        log.info("queue_fanout_02接收：{}", msg);
    }

    @RabbitListener(queues = "topicQueue01")
    public void topicReceive01(Object msg) {
        log.info("topicQueue01接收：{}", msg);
    }

    @RabbitListener(queues = "topicQueue02")
    public void topicReceive02(Object msg) {
        log.info("topicQueue02接收：{}", msg);
    }

    @RabbitListener(queues = "topicQueue03")
    public void topicReceive03(Object msg) {
        log.info("topicQueue03接收：{}", msg);
    }

    @RabbitListener(queues = "topicQueue04")
    public void topicReceive04(Object msg) {
        log.info("topicQueue04接收：{}", msg);
    }

    @RabbitListener(queues = "skillQueue")
    public void secKillReceive(String msg) {
        log.info("skillQueue已接收到：{}", msg);

        //处理消息，下单
        SecKillMessage secKillMessage = JsonUtil.jsonStr2Object(msg, SecKillMessage.class);
        if (secKillMessage != null) {
            Long goodsId = secKillMessage.getGoodsId();
            User user = secKillMessage.getUser();
            GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
            if (goodsVo == null) {
                return;
            }
            if (goodsVo.getStockCount() < 1) {
                return;
            }

            // 判断是否重复加购
            Order order = (Order) redisTemplate.opsForValue().get("secOrder:" + user.getId() + ":" + goodsId);
            if (order != null) {
                return;
            }

            // 下单
            order = orderService.secKill(user, goodsVo);

            //放到缓存中
            redisTemplate.opsForValue().set("secOrder:" + user.getId() + ":" + goodsId, order);
        }

    }
}
