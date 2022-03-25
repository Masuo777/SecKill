package com.marshio.redisdemo.vo;

import com.marshio.redisdemo.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author masuo
 * @data 18/3/2022 上午10:58
 * @Description 订单详情页数据对象，包含订单信息、用户、商品信息
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo extends Order {

    private String goodsImg;

}
