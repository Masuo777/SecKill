package com.marshio.redisdemo.controller;


import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.OrderService;
import com.marshio.redisdemo.vo.OrderDetailVo;
import com.marshio.redisdemo.vo.ResponseBean;
import com.marshio.redisdemo.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    private OrderService orderService;

    public OrderController(){}

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ResponseBean getOrderDetailByOrderId(User user, Long orderId) {
        if(user == null){
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }

        if(orderId == null){
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }

        OrderDetailVo orderDetailVo = orderService.getOrderDetailByOrderId(orderId);

        if(orderDetailVo == null){
            return ResponseBean.error(ResponseBeanEnum.ORDER_ERROR_NOTFOUND);
        }
        return ResponseBean.success(orderDetailVo);
    }

}
