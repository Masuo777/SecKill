package com.marshio.redisdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author masuo
 * @data 9/3/2022 上午8:33
 * @Description 公共返回对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBean {

    private long code;
    private String message;
    private Object objects;


    /**
     *
     * @return ResponseBean
     */
    public static ResponseBean success(){
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(),ResponseBeanEnum.SUCCESS.getMessage(),null);
    }

    public static ResponseBean success(Object obj){
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(),ResponseBeanEnum.SUCCESS.getMessage(),obj);
    }

    /**
     *
     * @param respBeanEnum ResponseBeanEnum
     * @return ResponseBean
     */
    public static ResponseBean error(ResponseBeanEnum respBeanEnum){
        return new ResponseBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),null);
    }

    /**
     *
     * @param respBeanEnum ResponseBeanEnum
     * @param obj Object
     * @return ResponseBean
     */
    public static ResponseBean error(ResponseBeanEnum respBeanEnum,Object obj){
        return new ResponseBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),obj);
    }
}
