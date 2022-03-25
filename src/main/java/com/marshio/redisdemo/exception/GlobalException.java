package com.marshio.redisdemo.exception;

import com.marshio.redisdemo.vo.ResponseBeanEnum;
import lombok.*;

/**
 * @author masuo
 * @data 9/3/2022 上午11:17
 * @Description 自定义异常类
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException{

    private ResponseBeanEnum responseBeanEnum;
}
