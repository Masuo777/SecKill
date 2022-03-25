package com.marshio.redisdemo.config;

import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.UserService;
import com.marshio.redisdemo.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author masuo
 * @data 11/3/2022 上午10:17
 * @Description 自定义用户参数解析器
 * 1、SpringMVC解析器用于解析request请求参数并绑定数据到Controller的入参上。
 * 2、自定义一个参数解析器需要实现HandlerMethodArgumentResolver接口，重写supportsParameter和resolveArgument方法，配置文件中加入resolver配置。
 * 3、如果需要多个解析器同时生效需要在一个解析器中对其他解析器做兼容。
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private UserService userService;

    public UserArgumentResolver() {
    }

    @Autowired
    public UserArgumentResolver(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        // Spring会拦截所有的进入controller的请求，在这个方法中判断request请求中的参数

        //判断入参是否为用户
        return methodParameter.getParameterType() == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        System.out.println("获取cookie，user Ticket为：" + userTicket);
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }

        return userService.getUserByCookie(request, response, userTicket);
    }
}
