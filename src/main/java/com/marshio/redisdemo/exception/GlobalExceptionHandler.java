package com.marshio.redisdemo.exception;

import com.marshio.redisdemo.vo.ResponseBean;
import com.marshio.redisdemo.vo.ResponseBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author masuo
 * @data 9/3/2022 上午11:29
 * @Description 全局异常处理类
 * RestControllerAdvice可以处理controller异常，但是不能处理未进入controller的异常
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*
     * 在spring中处理异常有两种方式
     *  1.@Controller与@ExceptionHandler结合
     *  2.@ControllerAdvice与@ExceptionHandler
     *  3.实现HandlerExceptionResolver接口
     *  4.Spring boot独特的异常处理，
     *      4.1：实现ErrorController
     */

    @ExceptionHandler(Exception.class)
    public ResponseBean exceptionHandler(Exception e){
        if(e instanceof GlobalException){
            GlobalException ge = (GlobalException) e;
            return ResponseBean.error(ge.getResponseBeanEnum());
        }else if (e instanceof BindException){
            BindException be = (BindException) e;
            ResponseBean error = ResponseBean.error(ResponseBeanEnum.BIND_ERROR);
            error.setMessage(error.getMessage()+ ":" + be.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return error;
        }else if(e instanceof MissingRequestCookieException){
            MissingRequestCookieException ex = (MissingRequestCookieException) e;
            return ResponseBean.error(ResponseBeanEnum.UNLOGIN_ERROR);
        }else if(e instanceof RuntimeException){
            RuntimeException ex = (RuntimeException) e;
            ResponseBean error = ResponseBean.error(ResponseBeanEnum.UNKNOWN_ERROR);
            error.setMessage(error.getMessage() + ":" + ex.getMessage());
            return error;
        }
        return ResponseBean.error(ResponseBeanEnum.ERROR);
    }
}
