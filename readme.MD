## j360-trace-all
跟踪监控插件
* 本项目供学习之用，参考Google Dapper论文设计，收集存储分离，使用kafka，http，mq等进行解耦


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
    - dubbo
    - httpClient
    - okhttp
    - mysql
    - mq
    - java service（javasist aspectj）暂未集成
