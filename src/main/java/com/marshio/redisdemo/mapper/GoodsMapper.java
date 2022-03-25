package com.marshio.redisdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marshio.redisdemo.pojo.Goods;
import com.marshio.redisdemo.vo.GoodsVo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
@Repository
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 获取商品列表
     * @return GoodsVo
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取秒杀商品信息
     * @param id 商品id
     * @return GoodsVo
     */
    GoodsVo findGoodsVoByGoodsId(Long id);
}
