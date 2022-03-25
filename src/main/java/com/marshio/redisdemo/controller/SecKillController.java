package com.marshio.redisdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.marshio.redisdemo.mq.RabbitMQSender;
import com.marshio.redisdemo.pojo.Order;
import com.marshio.redisdemo.pojo.SecKillMessage;
import com.marshio.redisdemo.pojo.SeckillOrder;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.GoodsService;
import com.marshio.redisdemo.service.OrderService;
import com.marshio.redisdemo.service.SeckillOrderService;
import com.marshio.redisdemo.utils.JsonUtil;
import com.marshio.redisdemo.vo.GoodsVo;
import com.marshio.redisdemo.vo.ResponseBean;
import com.marshio.redisdemo.vo.ResponseBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author masuo
 * @data 14/3/2022 上午10:08
 * @Description 秒杀控制器
 */


@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {

    private GoodsService goodsService;
    private OrderService orderService;
    private RabbitMQSender rabbitMQSender;
    private SeckillOrderService seckillOrderService;
    private RedisTemplate<String, Object> redisTemplate;

    // 内存标记
    private final Map<Long, Boolean> isEmpty = new HashMap<>();

    public SecKillController() {
    }

    @Autowired
    public SecKillController(GoodsService goodsService, SeckillOrderService seckillOrderService, OrderService orderService, RedisTemplate<String, Object> redisTemplate, RabbitMQSender rabbitMQSender) {
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
        this.rabbitMQSender = rabbitMQSender;
        this.seckillOrderService = seckillOrderService;
    }

    /**
     * 执行秒杀，动态请求
     *
     * @param model   数据
     * @param user    用户
     * @param goodsId 商品id
     * @return string
     */
    @RequestMapping("/doSecKillDynamic")
    public String doSecKillD(Model model, User user, Long goodsId) {

        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);

        //判断库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            //库存不足,秒杀失败
            model.addAttribute("error", ResponseBeanEnum.SKILL_ERROR_GOODS_SHORTAGE.getMessage());
            return "order/fail";
        }
        //判断订单，不允许重复加购,这里需要注意的是，不要使用前端的数量，而是使用数据库的数量
        SeckillOrder order = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (order != null) {
            model.addAttribute("error", ResponseBeanEnum.SKILL_ERROR_GOODS_LOP.getMessage());
            return "order/fail";
        }
        //执行抢购
        Order secOrder = orderService.secKill(user, goodsVo);


        model.addAttribute("order", secOrder);
        model.addAttribute("goods", goodsVo);
        return "order/detail";
    }

    /**
     * 执行秒杀，页面静态化的Ajax请求，将用户订单缓存到redis
     *
     * @param user    用户
     * @param goodsID 商品id
     * @return string
     */
    @RequestMapping(value = "/doSecKillWithRedis", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean doSecKillWithRedis(User user, Long goodsID) {

        if (user == null) {
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }
        // model.addAttribute("user",user);

        // 判断库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsID);
        if (goodsVo.getStockCount() < 1) {
            // 库存不足,秒杀失败
            return ResponseBean.error(ResponseBeanEnum.SKILL_ERROR_GOODS_SHORTAGE);
        }

        // 判断是否重复加购，优化一：加缓存
        Order order = (Order) redisTemplate.opsForValue().get("secOrder:" + user.getId());
        if (order != null) {
            return ResponseBean.error(ResponseBeanEnum.SKILL_ERROR_GOODS_LOP);
        }

        // 执行抢购
        order = orderService.secKill(user, goodsVo);

        //将订单存入redis，此操作应在控制层进行
        redisTemplate.opsForValue().set("order:" + user.getId(), order);

        return ResponseBean.success(order);
    }

    /**
     * 执行秒杀，页面静态化的Ajax请求，将用户订单缓存到redis，并使用RabbitMQ进行流量削峰
     * 之前我们对订单进行了缓存操作，加快了一定秒杀速度，接下来的优化方向就是对库存查询进行优化
     * 1、预减库存，通过将第一次查询到的数据库库存放到缓存中，然后再对缓存中的数据进行操作，在给数据库发异步信息更新数据库
     * 2、添加标记位，当库存预减为0后，我们仍需访问redis缓存，来获取缓存的库存数量，但是后面所有的请求都是多余，所以我们添加一个内存标记位，来标识缓存数量是否大于0
     * 3、消息异步，在执行完下单之后，成功生成订单则返回状态0，表示当前订单正在排队
     *
     * @param user    用户
     * @param goodsID 商品id
     * @return ResponseBean
     */
    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean doSecKillWithRedisAndRabbitMQ(User user, Long goodsID) {
        //判断用户是否登录
        if (user == null) {
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }

        //判断标记位
        if (isEmpty.get(goodsID)) {
            return ResponseBean.error(ResponseBeanEnum.SKILL_ERROR_GOODS_SHORTAGE);
        }

        //获取redis
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        // 判断是否重复加购
        Order order = (Order) operations.get("secOrder:" + user.getId() + ":" + goodsID);
        if (order != null) {
            return ResponseBean.error(ResponseBeanEnum.SKILL_ERROR_GOODS_LOP);
        }

        // 预减库存，实现InitializingBean,重写afterPropertiesSet方法

        // 预减库存操作 + 修改标记位
        Long stock = operations.decrement("secKillGoods:" + goodsID);

        if (stock != null && stock < 0) {
            operations.increment("secKillGoods:" + goodsID);
            isEmpty.put(goodsID, true);
            //库存不足
            return ResponseBean.error(ResponseBeanEnum.SKILL_ERROR_GOODS_SHORTAGE);
        }

        // 下单，rabbitMQ,
        SecKillMessage secKillMessage = new SecKillMessage(user, goodsID);
        // 流量削峰
        rabbitMQSender.sendSecKillMsg(JsonUtil.object2JsonStr(secKillMessage));

        // 返回0，页面显示正在排队ing
        return ResponseBean.success(0);
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBean getResult(User user, Long goodsId) {
        if (user == null) {
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }

        if (goodsId == null) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }

        // 先查询缓存、在查询数据库,因为缓存快，虽然订单先存储到数据库
        Order order = (Order) redisTemplate.opsForValue().get("secOrder:" + user.getId() + ":" + goodsId);
        if (order != null) {
            return ResponseBean.success(order.getId());
        }

        //缓存中没有，查询数据库
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("goods_id",goodsId));
        if (seckillOrder != null) {
            //再查询缓存
            order = (Order) redisTemplate.opsForValue().get("secOrder:" + user.getId() + ":" + goodsId);
            if (order != null) {
                return ResponseBean.success(order.getId());
            }
        }

        //数据库中也没有，返回排队中的状态
        return ResponseBean.success(0);
    }

    /**
     * 初始化，把商品数量加载到库存
     */
    @Override
    public void afterPropertiesSet() {
        //初始化执行的方法，需要把商品库存数量加载到redis
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            //轮询设置缓存，根据商品id（key），设置商品数量（value）
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());

            // 设置内存标计
            isEmpty.put(goodsVo.getId(), false);
        });

    }
}
