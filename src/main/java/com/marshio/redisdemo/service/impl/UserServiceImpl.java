package com.marshio.redisdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marshio.redisdemo.exception.GlobalException;
import com.marshio.redisdemo.mapper.UserMapper;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.service.UserService;
import com.marshio.redisdemo.utils.CookieUtil;
import com.marshio.redisdemo.utils.MD5Util;
import com.marshio.redisdemo.utils.UUIDUtil;
import com.marshio.redisdemo.vo.LoginVo;
import com.marshio.redisdemo.vo.ResponseBean;
import com.marshio.redisdemo.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author masuo
 * @since 2022-03-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private UserMapper userMapper;
    private RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl() {
    }

    @Autowired
    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        /*
         * 手动校验
         *         if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
         *             return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
         *         }
         *         if(MobileValidator.isMobile(mobile)){
         *             return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
         *         }
         */

        //根据手机号获取用户并校验
        User user = userMapper.selectById(mobile);
        if (user == null) {
            // return ResponseBean.error(ResponseBeanEnum.ACCOUNT_NOT_EXIST_ERROR);
            throw new GlobalException(ResponseBeanEnum.ACCOUNT_NOT_EXIST_ERROR);
        }

        //判断密码是否正确
        if (!MD5Util.formPassToDbPass(password, user.getSlat()).equals(user.getPassword())) {
            // return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
        }

        //登陆成功后生成cookie
        String ticket = UUIDUtil.uuid();
        System.out.println("uuid：" + ticket);
        //设置当前会话的 session id
        // request.getSession().setAttribute(ticket,user);

        //由于我们使用redis实现会话持久，我们不再将用户信息放入session,
        redisTemplate.opsForValue().set("userTicket:" + ticket, user);
        //设置cookie
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        //需要将生成的cookie
        return ResponseBean.success(ticket);
    }

    @Override
    public User getUserByCookie(HttpServletRequest request, HttpServletResponse response, String userTicket) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("userTicket:" + userTicket);
        if (user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    @Override
    public ResponseBean updatePassword(HttpServletRequest request, HttpServletResponse response, String userTicket, String password) {
        User user = getUserByCookie(request, response, userTicket);
        if (user == null) {
            return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
        }
        user.setPassword(password);
        int success = userMapper.updateById(user);
        if (1 == success) {
            //删除缓存
            redisTemplate.delete("userTicket:" + userTicket);

            //更新缓存
            redisTemplate.opsForValue().set("userTicket:" + userTicket, user);
            return ResponseBean.success();
        }
        return ResponseBean.error(ResponseBeanEnum.PASSWORD_UPDATE_ERROR);
    }

}
