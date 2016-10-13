## j360-trace-all
跟踪监控插件，开箱即用，代码整理源于zipkin,并整合实际场景中的应用 duboo,shiro等拦截监控,加入各种sample整合,使用方式参考module:j360-trace-example
* 本项目供学习之用，参考Google Dapper论文设计，收集存储分离，使用kafka，http，mq等进行解耦

* java监控和信息收集主要有以下几种实行
    - Agent形式，通常是用字节码修改技术+java.lang.instrument等方式，可以做到无侵入监控,参考pinpoint,BTrace
    - 过滤器、拦截器、aop aspectJ等配置化方式收集，配置化植入，可以无需修改代码(本方案采用) 参考zipkin+brave
    - 代码埋点植入，需要添加植入代码收集,参考cat、cat2

* 依赖
 - Maven3+
 - jdk7+


* log
 - 所有log输出统一到slf4j，实现使用logback

* 监控流程
 - 植入
 - 收集
 - 传输
 - 接收
 - 存储


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
    - log

* storage collector
    - kafka
    - httpserver
    - mq

* storage 存储
    - elasticsearch
    - mongodb


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
    - okhttp - spring环境中配置OkHttpClient bean
    - mysql - 在连接jdbc后添加?statementInterceptors=me.j360.trace.mysql.MySQLStatementInterceptor
    - mq
    - java service（javasist aspectj）暂未集成

* example
    - springmvc+filter
    - dubbo
    - springmvc-collector-kafkaProductor
    - springmvc-dubbo-collector-kafkaProductor
    - kafkaConsumerStream-storage-elasticsearch-native-start
    - kafkaConsumerStream-storage-elasticsearch-springboot-start


* 生产环境JEE中使用监控
 - 生产环境使用微服务设计方式,具体的Java容器部署包括:
    - tomcat/jetty 若干 http入口
    - jar 若干 基础+业务服务
    - dubbo相关服务
 - 单个http请求链路服务时序
    - filter 权限过滤等(shiro)
    - springmvc interceptor 部分拦截(log等)
    - 外部服务请求 http (httpClient/okhttp/restTemplate)
    - 一级微服务调用
    - 二级微服务调用
    - interceptor 返回
    - filter response确认
 - 请求链路分为
    - client请求方 负责发起,接收
    - server服务方 负责接收,返回
    - 相邻链路同为server时,只记录一次,比如shiro的filter直接过滤掉信息,则不转发到servlet中处理,如果成功,则以servlet为准,部分接口不经过shiro则直接servlet接收
