package com.marshio.redisdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author masuo
 * @data 8/3/2022 下午1:31
 * @Description 测试
 */

@Controller
@RequestMapping("/demo")
public class HelloController {

    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","xxxx");
        return "hello";
    }
}
