# 项目搭建

- springboot:2.2.2.RELEASE
- mysql:8.0
- redis:6.2.6
- rabbitmq:3.8-management

## 安装环节

略

## 数据准备

SQL文件位于resource/sql文件中。

## 配置文件

![image-20220325170520616](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220325170521.png)

修改redis、mysql、rabbitmq的服务器地址及用户信息



项目到此搭建完成。



# 服务优化

## 缓存商品列表页面

> 随着网用户数量的增加，假设网站已经达到其瓶颈。那么我们就需要思考如何优化系统。
>
> 优化思路：将页面与数据不需要经常改动的页面加入redis缓存。

**原代码**

```java
@RequestMapping("/toGoods")
public String toGoods(Model model) {
    model.addAttribute("goodsList", goodsService.findGoodsVo());
    // 动态获取页面并渲染数据
    return "goods/list";
}
```

> 动态渲染页面，每次该请求都会重新获取`ModelAndView`，重新渲染，如果在短时间内多次请求，页面基本不会发生太大的改变 ， 但是重复的请求会触发多次数据库查询等操作，费时费力，且没有好处.
>
> 此时我们可以思考如何加快用户的访问速度
>
> - 缓存，缓存加快了我们的访问速度，但是当页面数据发生变化时需要重新加载缓存
>
> - 将页面静态化，在访问页面时利用Ajax技术去动态获取数据并渲染

### 将页面存入Redis

首次访问时，手动渲染页面将其放到Redis缓存。

**优化之后的代码**

```java
/**
 * 将其缓存到redis，加快访问速度
 */
@RequestMapping(value = "/toGoods", produces = "text/html;charset=utf-8")
@ResponseBody
public String toGoodsStatic(User user, Model model, HttpServletRequest request, HttpServletResponse response) {
    //获取redis
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
```



## 静态化商品详情页

> 对于商品详情页面，整体的页面框架不会发生太大的改变，唯一需要经常改变的就是商品信息，即商品数据。对于此类页面，我们可以使用页面静态化来优化访问速度。

**原请求**

```java
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
```



**原页面**

```html
<!DOCTYPE html>
<html lang="en"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商品详情</title>
    <!-- jquery -->
    <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
    <!-- bootstrap -->
    <link rel="stylesheet" type="text/css" th:href="@{/bootstrap/css/bootstrap.min.css}"/>
    <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}"></script>
    <!-- layer -->
    <script type="text/javascript" th:src="@{/layer/layer.js}"></script>
    <!-- common.js -->
    <script type="text/javascript" th:src="@{/js/common.js}"></script>
</head>
<body>
<div class="panel panel-default">
    <div class="panel-heading">秒杀商品详情</div>
    <div class="panel-body">
        <span th:if="${user eq null}"> 您还没有登录，请登陆后再操作<br/></span>
        <span>没有收货地址的提示。。。</span>
    </div>
    <table class="table" id="goods">
        <tr>
            <td>商品名称</td>
            <td colspan="3" th:text="${goods.goodsName}"></td>
        </tr>
        <tr>
            <td>商品图片</td>
            <td colspan="3"><img th:src="@{${goods.goodsImg}}" width="200" height="200" alt="iphone" src=""/></td>
        </tr>
        <tr>
            <td>秒杀开始时间</td>
            <td th:text="${#dates.format(goods.startDate,'yyyy-MM-dd HH:mm:ss')}"></td>
            <td id="seckillTip">
                <input type="hidden" id="remainSeconds" th:value="${remainSeconds}">
                <span th:if="${state eq -1}">秒杀倒计时：<span id="countDownSec" th:text="${remainSeconds}"></span> 秒</span>
                <span th:if="${state eq 0}">秒杀正在进行中</span>
                <span th:if="${state eq 1}">秒杀已结束</span>
            </td>
            <td>
                <form id="secKillForm" method="post" action="/secKill/doSecKill">
                    <input type="hidden" name="goodsId" th:value="${goods.id}">
                    <button class="btn btn-primary btn-block" type="submit" id="secButton">立即秒杀</button>
                </form>
            </td>
        </tr>
        <tr>
            <td>商品原价</td>
            <td colspan="3" th:text="${goods.goodsPrice}"></td>
        </tr>
        <tr>
            <td>秒杀价</td>
            <td colspan="3" th:text="${goods.seckillPrice}"></td>
        </tr>
        <tr>
            <td>库存数量</td>
            <td colspan="3" th:text="${goods.stockCount}"></td>
        </tr>
    </table>
</div>
</body>
<script>
    //$(function () {}  <==>  $().ready(function () {}  <==>  $(document).ready(function () {}
    $(function () {
        countDown();
    });

    function countDown() {
        // 实现倒计时刷新
        const remainSeconds = $("#remainSeconds").val();
        // alert(remainSeconds);

        console.log(typeof remainSeconds);
        let timeout;
        if (remainSeconds > 0) {
            $('#secButton').attr("disabled",true);
            timeout = setTimeout(function () {
                $('#countDownSec').text(remainSeconds - 1);
                $("#remainSeconds").val(remainSeconds - 1);
                countDown();
            }, 1000);
        } else if (remainSeconds === '0') {
            $('#secButton').attr("disabled",false);
            console.log(timeout);
            if (timeout) {
                clearTimeout(timeout);
            }
            $("#seckillTip").html("秒杀进行中");
        } else {
            $('#secButton').attr("disabled",true);
            $("#seckillTip").html("秒杀已结束");
        }
    }

</script>
</html>
```

原页面基于thyme leaf的引擎，动态渲染页面。



**配置静态资源处理**

```yaml
  # 静态资源处理
  resources:
    # 自动默认静态资源处理，默认启用
    add-mappings: true
    cache:
      cachecontrol:
        # 缓存时间，单位秒
        max-age: 3600
    chain:
      # 资源自动缓存，默认启用
      cache: true
      # 启用资源链 ，默认禁用
      enabled: true
      # 压缩资源，默认禁用，如：gzip
      compressed: true
      # H5的默认缓存，默认禁用
      html-application-cache: true
    # 静态资源位置
    static-locations: classpath:/static/
```



**静态资源配置类**

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
	// controller入参解析器
    private final UserArgumentResolver userArgumentResolver;

    public WebConfig(UserArgumentResolver userArgumentResolver){
        this.userArgumentResolver = userArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 添加用户入参解析器
        resolvers.add(userArgumentResolver);
    }
	// 静态资源处理器
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
```



**静态化请求**

**templates/goods/list.html**

```html
<td><a th:href="'/view/goods/detail.htm?goodsID='+${goods.id}">详情</a></td>
```



**静态化页面**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>商品详情</title>
    <!-- jquery -->
    <script type="text/javascript" src="../../js/jquery.min.js"></script>
    <!-- bootstrap -->
    <link rel="stylesheet" type="text/css" href="../../bootstrap/css/bootstrap.min.css"/>
    <script type="text/javascript" src="../../bootstrap/js/bootstrap.min.js"></script>
    <!-- layer -->
    <script type="text/javascript" src="../../layer/layer.js"></script>
    <!-- common.js -->
    <script type="text/javascript" src="../../js/common.js"></script>
</head>
<body>
<div class="panel panel-default">
    <div class="panel-heading">秒杀商品详情</div>
    <div class="panel-body">
        <span id="userTip"> 您还没有登录，请登陆后再操作<br/></span>
        <span>没有收货地址的提示。。。</span>
    </div>
    <table class="table" id="goods">
        <tr>
            <td>商品名称</td>
            <td colspan="3" id="goodsName"></td>
        </tr>
        <tr>
            <td>商品图片</td>
            <td colspan="3"><img id="goodsImg" width="200" height="200" alt="iphone" src=""/></td>
        </tr>
        <tr>
            <td>秒杀开始时间</td>
            <td id="goods_secKill_startTime"></td>
            <td>
                <input type="hidden" id="remainSeconds">
                <span id="secKillTip"></span>
                <span id="countDownSec"></span>
                <span id="seconds">秒</span>
            </td>
            <td>
                <!--<form id="secKillForm" method="post" action="/secKill/doSecKill">-->
                <!--    <input type="hidden" name="goodsId" id="goodsID">-->
                <!--    <button class="btn btn-primary btn-block" type="submit" id="secButton">立即秒杀</button>-->
                <!--</form>-->
                <button class="btn btn-primary btn-block" type="button" id="buyButton" onclick="doSecKill()">立即秒杀
                    <input type="hidden" name="goodsId" id="goodsID">
                </button>
            </td>
        </tr>
        <tr>
            <td>商品原价</td>
            <td colspan="3" id="goodsPrice"></td>
        </tr>
        <tr>
            <td>秒杀价</td>
            <td colspan="3" id="secKillPrice"></td>
        </tr>
        <tr>
            <td>库存数量</td>
            <td colspan="3" id="stockCount"></td>
        </tr>
    </table>
</div>
</body>
<script>
    //$(function () {}  <==>  $().ready(function () {}  <==>  $(document).ready(function () {}
    $(function () {
        //页面加载完成之后执行，比ready先执行
        getGoodsDetail();
    });

    var goodsDetailVo;

    //请求商品详情
    function getGoodsDetail() {
        let goodsID = g_getQueryString('goodsID');
        $.ajax({
            url: '/goods/detail/' + goodsID,
            type: 'GET',
            success: function (data) {
                // console.log(data);
                if (data.code == 200) {
                    // 获取传递的VO对象
                    goodsDetailVo = data.objects;

                    // 用户是否登录
                    let user = goodsDetailVo.user;
                    if (user != null) {
                        $("#userTip").hide();
                    }
                    // 给商品信息赋值
                    let goodsVo = goodsDetailVo.goodsVo;
                    $("#goodsName").text(goodsVo.goodsName);
                    $("#goodsImg").attr('src', goodsVo.goodsImg);
                    $("#goodsID").val(goodsVo.id);
                    // 格式化时间
                    $("#goods_secKill_startTime").text(new Date(goodsVo.startDate).format('yyyy-MM-dd HH:mm:ss'));
                    $("#goodsPrice").text(goodsVo.goodsPrice);
                    $("#secKillPrice").text(goodsVo.seckillPrice);
                    $("#stockCount").text(goodsVo.stockCount);
                    // 秒杀活动状态，剩余时间
                    let remainSeconds = goodsDetailVo.remainSeconds;
                    let state = goodsDetailVo.state;

                    // console.log("state:"+state);
                    if (state === -1) {
                        // 秒杀未开始
                        $("#secKillTip").text("秒杀未开始，还剩");
                        $("#countDownSec").text(remainSeconds);
                        $("#countDownSec").css('display', 'block');
                        $("#remainSeconds").val(remainSeconds);
                        $("#remainSeconds").css('display', 'block');
                        $("#seconds").show();
                        // 启用计时器
                        countDown();
                    } else if (state == 0) {
                        // 秒杀进行中
                        $("#secKillTip").text("秒杀进行中");
                        $("#countDownSec").css('display', 'none');
                        $("#seconds").hide();
                    } else if (state == 1) {
                        // 秒杀已结束
                        $("#secKillTip").text("秒杀已结束");
                        $("#countDownSec").css('display', 'none');
                        $("#seconds").hide();
                        $("#secButton").attr("disabled", true);
                    }

                } else {
                    layer.msg(data.message);
                }
            },
            error: function (data) {
                layer.msg(data.message);
            }
        })
    }

    //日期格式化, pattern 日期模式
    Date.prototype.format = function (pattern) {
        //声明fullDate
        const fullDate = {
            // 获取四位的年（yyyy）
            'yyyy': this.getFullYear().toString(),
            // getMonth() ==> 获取月（0-11）,需要+1
            'MM': (this.getMonth() + 1).toString(),
            // 以数值返回天（1-31）
            'dd': this.getDate().toString(),

            // 获取小时（0-23）
            'HH': this.getHours().toString(),
            // 获取分（0-59）
            'mm': this.getMinutes().toString(),
            // 获取秒（0-59）
            'ss': this.getSeconds().toString()
            //TODO 添加其他格式的时间
        }
        // 替换pattern里的匹配项
        for (let f in fullDate) {
            // alert(f); yyyy,在这里只能通过构造函数将变量传入，因为字面量会将一切字符当作匹配项
            if (new RegExp("(" + f + ")").test(pattern)) {
                // console.log(f);
                // console.log(fullDate[f].length);
                pattern = pattern.replace(RegExp.$1, fullDate[f].length === 1 ? "0" + fullDate[f] : fullDate[f]);
            }
        }
        return pattern;
    }

    function countDown() {
        // 实现倒计时刷新
        const remainSeconds = $("#remainSeconds").val();

        // console.log(typeof remainSeconds);
        let timeout;
        if (remainSeconds > 0) {
            console.log(remainSeconds);
            $('#secButton').attr("disabled", true);
            timeout = setTimeout(function () {
                $('#countDownSec').text(remainSeconds - 1);
                $("#remainSeconds").val(remainSeconds - 1);
                countDown();
            }, 1000);
        } else if (remainSeconds === '0') {
            $('#secButton').attr("disabled", false);
            console.log("");
            if (timeout) {
                clearTimeout(timeout);
            }
            $("#countDownSec").css('display', 'none');
            $("#seconds").hide();
            $("#secKillTip").html("秒杀进行中");
        } else {
            $("#countDownSec").css('display', 'none');
            $("#seconds").hide();
            $('#secButton').attr("disabled", true);
            $("#secKillTip").html("秒杀已结束");
        }
    }
    function doSecKill() {}
</script>
</html>
```



**GoodsController.java**

```java
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
```



其余页面同上。



## 异步通信优化

**pom文件添加rabbitmq依赖**

```xml
<!--AMQP依赖，消息中间件-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
    <!--注意这里可以不写version，maven会上查parent中的版本-->
</dependency>
```



**配置rabbitmq**

采用主题模式进行消息通信

```java
@Configuration
public class RabbitMQTopicConfig {
    /**
     * 主题模式支持通配符
     * 1、* ：匹配路由键的一个词（ 》= 1）
     * 2、# ：匹配路由键的一个或多个词（ 》= 0）
     */

    // 秒杀相关

    private static final String SKILL_QUEUE = "skillQueue";
    private static final String SKILL_EXCHANGE = "skillExchange";

    @Bean
    public Queue secKillQueue() {
        return new Queue(SKILL_QUEUE);
    }

    @Bean
    public TopicExchange secKillExchange() {
        return new TopicExchange(SKILL_EXCHANGE);
    }

    @Bean
    public Binding bindSecKill() {
        return BindingBuilder.bind(secKillQueue()).to(secKillExchange()).with("secKill.#");
    }
}
```



**RabbitMQReceiver.java**

```java
@Service
@Slf4j
public class RabbitMQReceiver {
    private OrderService orderService;
    private GoodsService goodsService;
    private RedisTemplate<String, Object> redisTemplate;

    public RabbitMQReceiver() {
    }

    @Autowired
    public RabbitMQReceiver(GoodsService goodsService, RedisTemplate<String, Object> redisTemplate, OrderService orderService) {
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "skillQueue")
    public void secKillReceive(String msg) {
        log.info("skillQueue已接收到：{}", msg);

        //处理消息，下单
        SecKillMessage secKillMessage = JsonUtil.jsonStr2Object(msg, SecKillMessage.class);
        if (secKillMessage != null) {
            Long goodsId = secKillMessage.getGoodsId();
            User user = secKillMessage.getUser();
            GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
            if (goodsVo == null) {
                return;
            }
            if (goodsVo.getStockCount() < 1) {
                return;
            }

            // 判断是否重复加购
            Order order = (Order) redisTemplate.opsForValue().get("secOrder:" + user.getId() + ":" + goodsId);
            if (order != null) {
                return;
            }

            // 下单
            order = orderService.secKill(user, goodsVo);

            //放到缓存中
            redisTemplate.opsForValue().set("secOrder:" + user.getId() + ":" + goodsId, order);
        }
    }
}

```



**RabbitMQSender.java**

```java
@Service
@Slf4j
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;

    public RabbitMQSender() {
    }

    @Autowired
    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendSecKillMsg(Object msg){
        log.info("skillExchange发送：{}",msg);
        rabbitTemplate.convertAndSend("skillExchange","secKill",msg);
    }
}

```



**秒杀接口优化**

```java
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
```





