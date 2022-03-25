package com.marshio.redisdemo.annonation;

import com.marshio.redisdemo.vo.MobileValidatorVo;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author masuo
 * @data 9/3/2022 上午10:59
 * @Description 手机号校验
 */

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        // 自定义校验规则
        validatedBy = {MobileValidatorVo.class}
)
public @interface Mobile {

    boolean required() default true;

    String message() default "手机号码格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
