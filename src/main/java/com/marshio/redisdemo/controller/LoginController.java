package com.marshio.redisdemo.controller;


import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.UserService;
import com.marshio.redisdemo.vo.LoginVo;
import com.marshio.redisdemo.vo.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


/**
 * @author masuo
 * @data 9/3/2022 上午8:25
 * @Description 登陆跳转
 */
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    /**
     * 注意，在spring4开始就不再推荐使用注解注入Field，推荐构造器和setter注入
     * Field注入应该尽可能地去避免使用
     */
    private UserService userService;

    public LoginController(){}

    /**
     * 构造器注入,这里再注入对象时，spring容器会自动将对象注入
     * 使用构造器注入的好处就是对象可以声明为final，在实例化期间被初始化
     * 但是在构造器中有太多对象则违反了单一职责原则，更多的对象意味着更多的责任
     * 构造函数注入适合于强制依赖
     */
    @Autowired
    public LoginController(UserService userService){
        this.userService = userService;
    }

    /*
     * 基于setter注入，
     * spring容器会在实例化此controller对象时，为了注入controller的依赖对象，会自动调用setter函数
     * setter注入适合于可选依赖项
     */
    // public void setUserService(UserService userService){
    //     this.userService = userService;
    // }

    /**
     * 登录
     * @param user 容器DI
     * @return String
     */
    @RequestMapping("/toLogin")
    public String toLogin(User user) {
        // DONE 在用户登陆后，再次进入登陆页面时，需要判断用户是否登录，已登录过的用户无需再次登录
        if (user != null) {
            return "redirect:/goods/toGoods";
        }
        return "login";
    }

    /**
     * 登录
     * @param loginVo ，添加校验，使用spring boot自带的校验器注解，但是具体的校验规则需要自己完善
     * @return ResponseBean
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public ResponseBean doLogin(@Valid LoginVo loginVo,HttpServletRequest request,HttpServletResponse response) {
        log.info("{}",loginVo);
        return userService.doLogin(loginVo,request,response);
    }
}
