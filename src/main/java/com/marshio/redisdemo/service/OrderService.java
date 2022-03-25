package com.marshio.redisdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marshio.redisdemo.pojo.Order;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.vo.GoodsVo;
import com.marshio.redisdemo.vo.OrderDetailVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
public interface OrderService extends IService<Order> {

    /**
     * @param user    当前登录用户
     * @param goodsVo 秒杀商品信息
     * @return 生成的订单
     */
    Order secKill(User user, GoodsVo goodsVo);

    OrderDetailVo getOrderDetailByOrderId(Long orderId);
}
