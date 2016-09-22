## j360-trace-all
跟踪监控插件
* 本项目供学习之用，参考Google Dapper论文设计，收集存储分离，使用kafka，http，mq等进行解耦

* log
 - 所有log输出统一到slf4j，实现使用logback

* 集成时版本注意事项
 - dubbo version
 - log version
 - spring version
 - javasist version
 - netty version


##module组成
* collector 收集
    - kafka
    - http
    - mq
    - local（logAppender接口）
* storage 存储
    - mongodb
    - log（console，file）


* trace点
    - springmvc
        - url = http://localhost:8080/ping/sync
        * headers
            - X-B3-Sampled true
            - X-B3-TraceId 1
            - X-B3-SpanId 1
            - X-B3-ParentSpanId 0
    - dubbo
        * Server(Provider)
            - dubbo:    <dubbo:provider filter="j360Provider"/>
            - UserServiceImpl
        * Client(Consumer)
            - dubbo:    <dubbo:provider filter="j360Consumer"/>
            - UserController -> UserService - > ↓
            - result =  _1  < - getUserName(uid)
    - httpClient
    - okhttp
    - mysql
    - mq
    - java service（javasist aspectj）暂未集成
