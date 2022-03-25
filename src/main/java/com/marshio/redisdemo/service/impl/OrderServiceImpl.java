package com.marshio.redisdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marshio.redisdemo.mapper.OrderMapper;
import com.marshio.redisdemo.pojo.Order;
import com.marshio.redisdemo.pojo.SeckillGoods;
import com.marshio.redisdemo.pojo.SeckillOrder;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.OrderService;
import com.marshio.redisdemo.service.SeckillGoodsService;
import com.marshio.redisdemo.service.SeckillOrderService;
import com.marshio.redisdemo.vo.GoodsVo;
import com.marshio.redisdemo.vo.OrderDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private OrderMapper orderMapper;
    private SeckillGoodsService seckillGoodsService;
    private SeckillOrderService seckillOrderService;


    public OrderServiceImpl() {
    }

    @Autowired
    public OrderServiceImpl(SeckillGoodsService seckillGoodsService, OrderMapper orderMapper, SeckillOrderService seckillOrderService) {
        this.orderMapper = orderMapper;

        this.seckillOrderService = seckillOrderService;
        this.seckillGoodsService = seckillGoodsService;
    }

    @Transactional
    @Override
    public Order secKill(User user, GoodsVo goodsVo) {
        //获取秒杀商品
        SeckillGoods goods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        goods.setStockCount(goods.getStockCount() - 1);

        /*
         * 在秒杀时确定了可以秒杀，就直接数量-1，这是不对的，因为没有考虑并发性，
         * 如果同时有多个线程请求此方法，同时查询数据库，由于在查询时数据库不会加锁
         * 所以多个线程查询到的数据是一样的，此时他们都符合秒杀条件，且库存保证了他们是可以抢购的
         * 此时对数据库数量-1，由于更新操作是加锁的，所以他们会逐一对数据库数据-1，就导致了超卖现象
         */
        // seckillGoodsService.updateById(goods);

        //更新商品数量，将更新操作，与比较合并为一条SQL语句，具有原子性，UpdateWrapper
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count - 1").eq("goods_id", goodsVo.getId()).gt("stock_count", 0));

        /*
         * 需要说一下，在这里不加锁的原因是，在高并发情况下，加锁是很想影响性能的。
         */
        if (!result) {
            return null;
        }
        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        //插入，插入后会将自动生成的订单ID注入到对象
        orderMapper.insert(order);

        //查看生成的order
        log.info("{}", order);

        //将订单存入redis

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        //插入
        seckillOrderService.save(seckillOrder);

        // seckillOrderService.
        return order;
    }

    @Override
    public OrderDetailVo getOrderDetailByOrderId(Long orderId) {

        if (orderId == null) {
            return null;
        }
        return orderMapper.selectOrderDetailByOrderId(orderId);
    }
}
