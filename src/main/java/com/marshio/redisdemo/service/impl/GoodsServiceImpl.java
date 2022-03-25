package com.marshio.redisdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marshio.redisdemo.mapper.GoodsMapper;
import com.marshio.redisdemo.pojo.Goods;
import com.marshio.redisdemo.service.GoodsService;
import com.marshio.redisdemo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private GoodsMapper goodsMapper;

    public GoodsServiceImpl(){}

    @Autowired
    public GoodsServiceImpl(GoodsMapper goodsMapper){
        this.goodsMapper = goodsMapper;
    }
    @Override
    public List<GoodsVo> findGoodsVo() {

        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(Long id) {
        return goodsMapper.findGoodsVoByGoodsId(id);
    }
}
