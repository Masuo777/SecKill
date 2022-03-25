package com.marshio.redisdemo.controller;


import com.marshio.redisdemo.mq.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author masuo
 * @since 2022-03-08
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private RabbitMQSender rabbitMqSender;

    public UserController(){}

    @Autowired
    public UserController(RabbitMQSender rabbitMqSender){
        this.rabbitMqSender = rabbitMqSender;
    }

    /**
     * 发送消息（测试）
     */
    @RequestMapping(value = "/mq", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String mq(){
        rabbitMqSender.send("Hello");
        return "发送成功";
    }

    /**
     * 发送消息（测试）
     */
    @RequestMapping(value = "/topic01", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String topic01(){
        rabbitMqSender.topicSend01("Hello");
        return "发送成功";
    }

    /**
     * 发送消息（测试）
     */
    @RequestMapping(value = "/topic02", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String topic02(){
        rabbitMqSender.topicSend02("topic02");
        return "发送成功";
    }

    /**
     * 发送消息（测试）
     */
    @RequestMapping(value = "/topic03", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String topic03(){
        rabbitMqSender.topicSend03("topic03");
        return "发送成功";
    }

    /**
     * 发送消息（测试）
     */
    @RequestMapping(value = "/topic04", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String topic04(){
        rabbitMqSender.topicSend04("topic04");
        return "发送成功";
    }

}
