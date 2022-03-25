package com.marshio.redisdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author masuo
 * @data 9/3/2022 上午8:34
 * @Description 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResponseBeanEnum {
    // 通用
    SUCCESS(200,"success"),
    ERROR(500,"服务端异常"),

    //登录模块
    UNLOGIN_ERROR(500110,"用户未登录"),
    LOGIN_ERROR(500210,"用户名或密码不正确"),
    MOBILE_ERROR(500211,"手机号码格式不正确"),
    ACCOUNT_NOT_EXIST_ERROR(500310,"用户不存在"),
    BIND_ERROR(500410,"参数校验异常"),
    PASSWORD_UPDATE_ERROR(500510,"用户密码修改失败"),

    //秒杀模块
    SKILL_ERROR_GOODS_SHORTAGE(600100,"库存不足"),
    //Limit one purchase
    SKILL_ERROR_GOODS_LOP(600100,"限购一件"),


    //订单模块
    ORDER_ERROR_NOTFOUND(600100,"未找到订单信息"),
    //未知错误
    UNKNOWN_ERROR(900100,"未知错误"),
    ;

    private final Integer code;
    private final String message;
}
