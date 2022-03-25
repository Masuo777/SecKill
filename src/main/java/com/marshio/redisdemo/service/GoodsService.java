package com.marshio.redisdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marshio.redisdemo.pojo.Goods;
import com.marshio.redisdemo.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 获取商品列表
     * @return goodsvo
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取秒杀商品信息
     * @param id 商品id
     * @return GoodsVo
     */
    GoodsVo findGoodsVoByGoodsId(Long id);
}
