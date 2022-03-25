package com.marshio.redisdemo.controller;

import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.GoodsService;
import com.marshio.redisdemo.vo.GoodsDetailVo;
import com.marshio.redisdemo.vo.GoodsVo;
import com.marshio.redisdemo.vo.ResponseBean;
import com.marshio.redisdemo.vo.ResponseBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author masuo
 * @data 9/3/2022 下午1:40
 * @Description 商品控制器
 */
@Slf4j
@Controller
@RequestMapping("/goods")
public class GoodsController {

    private GoodsService goodsService;

    private RedisTemplate<String, Object> redisTemplate;

    private ThymeleafViewResolver thymeleafViewResolver;

    public GoodsController() {
    }

    @Autowired
    public GoodsController(GoodsService goodsService, RedisTemplate<String, Object> redisTemplate, ThymeleafViewResolver thymeleafViewResolver) {
        this.goodsService = goodsService;
        this.redisTemplate = redisTemplate;
        this.thymeleafViewResolver = thymeleafViewResolver;
    }

    /*
     * 动态渲染页面，每次该请求都会重新获取ModelAndView，重新渲染，
     * 如果在短时间内多次请求，页面基本不会发生太大的改变 ，
     * 但是重复的请求会触发多次数据库查询等操作，费时费力，且没有好处
     *
     * 此时我们可以思考如何加快用户的访问速度
     * 1、缓存
     *  缓存加快了我们的访问速度，但是需要在用户首次访问时手动渲染页面
     *
     * 2、将页面静态化，在访问页面时利用Ajax技术去动态获取数据并渲染
     */

    /**
     * 展示商品列表页面，TPS = QPS
     *
     * @param model 数据模型
     * @return s
     */
    @RequestMapping("/toGoodsDynamic")
    public String toGoodsDynamic(User user, Model model) {

        // if (StringUtils.isEmpty(ticket)) {
        //     // 首先判断用户是否登录，如果没有登陆则跳转到登陆页面
        //     return "login";
        // }

        // User user = (User) session.getAttribute(ticket);

        // 通过userService获取用户信息
        // User user = userService.getUserByCookie(request, response, ticket);

        if (user == null) {
            log.info("{}", "用户未登录");
            return "redirect:/login/toLogin";
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goods/list";
    }

    /**
     * 将其缓存到redis，加快访问速度
     *
     * @param user     spring DI
     * @param model    spring DI
     * @param request  spring DI
     * @param response spring DI
     * @return String
     */
    @RequestMapping(value = "/toGoods", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toGoodsStatic(User user, Model model, HttpServletRequest request, HttpServletResponse response) {
        //获取
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        //判断redis是否有缓存
        String html = (String) operations.get("goods:detail");
        //有缓存，直接返回
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        //提前准备好model数据
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        //没有缓存，则需要手动渲染页面

        //准备WebContext
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        //手动渲染页面
        html = thymeleafViewResolver.getTemplateEngine().process("goods/list", webContext);

        if (!StringUtils.isEmpty(html)) {
            //将页面放入redis,并设置过期时间为60s
            operations.set("goods:detail", html, 60L, TimeUnit.SECONDS);
        }
        return html;
    }

    /*
     * 我们每次跳转时都需要判断用户是否登录，这是很麻烦的，我们需要优化
     * 优化方法就是在调用controller方法时，根据参数类型自动注入对象，
     * 需要实现HandlerMethodArgumentResolver方法
     * 然后判断对象是否登录
     */

    /**
     * 商品详情，动态获取
     *
     * @param id    商品id
     * @param model ModelAndView的model
     * @param user  用户
     * @return 页面
     */
    @RequestMapping("/toDetailDynamic/{id}")
    public String toDetailDynamic(@PathVariable("id") Long id, Model model, User user) {

        if (user == null) {
            return "/login";
        }

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(id);
        //获取秒杀开始时间
        Date startDate = goodsVo.getStartDate();
        //获取秒杀结束时间
        Date endDate = goodsVo.getEndDate();
        //获取当前时间
        Date now = new Date();
        //秒杀活动状态 -1（未开始）  0（进行中）  1（已结束）
        int state = 0;
        //倒计时，-1代表已开始或已结束
        int remainSeconds = 0;
        if (now.before(startDate)) {
            //秒杀未开始
            state = -1;
            //计算倒计时时间
            remainSeconds = (int) ((startDate.getTime() - System.currentTimeMillis()) / 1000);

        } else if (now.after(endDate)) {
            //秒杀已结束
            state = 1;
            remainSeconds = -1;
        }
        // System.out.println("state:" + state);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("state", state);
        model.addAttribute("goods", goodsVo);
        model.addAttribute("user", user);


        return "goods/detail";
    }

    /*
     * 类似详情页面，其结构是不会发生改变的，发生改变的呢只有数据
     * 所以我们需要将页面静态化，然后使用Ajax动态加载数据
     */

    /**
     * 商品详情，动态获取
     *
     * @param id   商品id
     * @param user 用户
     * @return 页面
     */
    @RequestMapping("/detail/{id}")
    @ResponseBody
    public ResponseBean toDetail(@PathVariable("id") Long id, User user) {
        if (user == null) {
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(id);
        //获取秒杀开始时间
        Date startDate = goodsVo.getStartDate();
        //获取秒杀结束时间
        Date endDate = goodsVo.getEndDate();
        //获取当前时间
        Date now = new Date();
        //秒杀活动状态 -1（未开始）  0（进行中）  1（已结束）
        int state = 0;
        //倒计时，-1代表已开始或已结束
        int remainSeconds = 0;
        if (now.before(startDate)) {
            //秒杀未开始
            state = -1;
            //计算倒计时时间
            remainSeconds = (int) ((startDate.getTime() - System.currentTimeMillis()) / 1000);

        } else if (now.after(endDate)) {
            //秒杀已结束
            state = 1;
            remainSeconds = -1;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setState(state);
        goodsDetailVo.setUser(user);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        return ResponseBean.success(goodsDetailVo);
    }
}