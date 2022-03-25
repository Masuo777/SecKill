package com.marshio.redisdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.vo.LoginVo;
import com.marshio.redisdemo.vo.ResponseBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author masuo
 * @since 2022-03-08
 */
public interface UserService extends IService<User> {

    /**
     * 登录功能
     * @param loginVo 登陆对象
     * @param request 请求体
     * @param response 响应体
     * @return ResponseBean
     */
    ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据用户ticket获取用户信息
     *
     * @param request 请求体
     * @param response 响应体
     * @param userTicket redis key
     * @return User
     */
    User getUserByCookie(HttpServletRequest request,HttpServletResponse response,String userTicket);

    /**
     * 修改密码,由于我们将用户信息存到了redis缓存中，这虽然加快了用户访问速度，但也带来了一些问题
     * 比如：用户密码发生改变，但是缓存没有发生改变，此时从缓存中获取到的数据就是错误的
     * 解决方法
     *  1、在用户更新信息之后删除缓存/更新缓存
     * @param request 请求体
     * @param response 响应体
     * @param userTicket 用户ticket
     * @param password 密码
     * @return ResponseBean
     */
    ResponseBean updatePassword(HttpServletRequest request, HttpServletResponse response, String userTicket,String password);
}
