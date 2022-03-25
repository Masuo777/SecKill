package com.marshio.redisdemo.vo;

import com.marshio.redisdemo.annonation.Mobile;
import com.marshio.redisdemo.validator.MobileValidator;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author masuo
 * @data 9/3/2022 上午11:01
 * @Description 手机号校验规则
 */

public class MobileValidatorVo implements ConstraintValidator<Mobile,String> {

    private boolean required = false;

    @Override
    public void initialize(Mobile mobile) {
        required = mobile.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return MobileValidator.isMobile(value);
        }else {
            if(StringUtils.isEmpty(value)){
                return true;
            }else {
                return MobileValidator.isMobile(value);
            }
        }
    }
}
