package com.marshio.redisdemo.vo;

import com.marshio.redisdemo.annonation.Mobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author masuo
 * @data 9/3/2022 上午9:14
 * @Description 登录对象
 */

@Data
public class LoginVo {

    @NotNull
    @Mobile
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
