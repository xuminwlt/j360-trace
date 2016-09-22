## j360-trace-all
跟踪监控插件
* 本项目供学习之用，参考Google Dapper论文设计，收集存储分离，使用kafka，http，mq等进行解耦

* java监控和信息收集主要有以下几种实行
    - Agent形式，通常是用字节码修改技术+java.lang.instrument等方式，可以做到无侵入监控
    - 过滤器、拦截器、aop aspectJ等配置化方式收集，配置化植入，可以无需修改代码(本方案采用)
    - 代码埋点植入，需要添加植入代码收集

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
