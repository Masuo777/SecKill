package com.marshio.redisdemo.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * @author masuo
 * @data 8/3/2022 下午3:18
 * @Description MD5相关
 */

@Component
public class MD5Util {

    private static final String SALT = "marshio106";

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    public static String inputPassToFormPass(String inputPass){
        String str = "" + SALT.charAt(0) + SALT.charAt(2) + inputPass + SALT.charAt(1)+ SALT.charAt(5);
        return md5(str);
    }

    public static String formPassToDbPass(String formPass,String salt){
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(1)+salt.charAt(5);
        return md5(str);
    }

    public static String inputPassToDbPass(String inputPass,String salt){
        String formPass = inputPassToFormPass(inputPass);
        return formPassToDbPass(formPass, salt);
    }

    public static void main(String[] args) {
        //4557944bc8337cfdd3adc48f71e358d2
        System.out.println(inputPassToFormPass("111111"));

        System.out.println(formPassToDbPass("3df53e83e851611eaefee899ca9658b7","marshio106"));

        System.out.println(inputPassToDbPass("111111","marshio106"));
    }
}
