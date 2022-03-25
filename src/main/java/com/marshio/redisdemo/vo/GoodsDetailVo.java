package com.marshio.redisdemo.vo;

import com.marshio.redisdemo.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author masuo
 * @data 17/3/2022 上午10:26
 * @Description 商品详情页所需数据，从controller传向view层的数据,View Object，视图层对象
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsDetailVo {

    private GoodsVo goodsVo;
    private User user;
    private int state;
    private int remainSeconds;
}
