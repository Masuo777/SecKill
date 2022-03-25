package com.marshio.redisdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marshio.redisdemo.pojo.Order;
import com.marshio.redisdemo.vo.OrderDetailVo;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
public interface OrderMapper extends BaseMapper<Order> {

    OrderDetailVo selectOrderDetailByOrderId(Long orderId);
}
