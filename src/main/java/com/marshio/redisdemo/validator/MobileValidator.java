package com.marshio.redisdemo.validator;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author masuo
 * @data 9/3/2022 上午9:49
 * @Description 手机号码校验
 */

public class MobileValidator {

    /**
     * 151开头的号码
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^151\\d{8}$");

    public static boolean isMobile(String mobile){
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        Matcher matcher = MOBILE_PATTERN.matcher(mobile);
        return matcher.matches();
    }
}
