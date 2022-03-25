# 压测



- QPS：query / per second

    处理流程（与TPS类似，但是一个TPS可能包含多个QPS，即一次请求多次查询）

    - 

- TPS：transation / per second

    处理流程：

    - 用户请求服务器
    - 服务器对请求进行处理
    - 服务器将处理好的数据返回

# JMeter

第一步、添加线程组

![image-20220314155619738](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314155649.png)

第二步、设置线程组信息

![image-20220314155909282](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314155909.png)

第三步、添加配置元件

![image-20220314160034696](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314160034.png)

第四步、设置默认值

![image-20220314160203907](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314160203.png)

第五步、添加取样器

![image-20220314160259420](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314160259.png)

第六步、添加监听

![image-20220314163121395](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220314163121.png)

第七步、运行

如果发现结果树的返回数据为500，则查看服务端控制器报的错。
如果发现结果树的返回数据为登录页面，则需添加请求头，

步骤如下：

第八步、添加请求头

![image-20220315162823184](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220315162823.png)

第九步、配置请求头

需要在请求成功的页面上打开“F12”，找到“Network”，找到相应的请求，找到“Request Header”，点击“View Source”，复制。

![image-20220315163122178](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220315163122.png)

第十步、添加到JMeter

打开信息头管理器，点击从粘贴板添加。

运行即可

![image-20220315163251798](https://masuo-github-image.oss-cn-beijing.aliyuncs.com/image/20220315163251.png)

再次运行查看结果树，响应体为正常响应界面。