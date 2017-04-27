package me.j360.trace.example.dubbo.service;

import me.j360.trace.collector.core.Brave;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Package: me.j360.trace.example.dubbo.service
 * User: min_xu
 * Date: 16/9/22 下午2:43
 * 说明：
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private Brave brave;

    @Override
    public String getUserName(Long uid) {

        //添加额外的Span信息,替换log.info
        brave.serverTracer().submitBinaryAnnotation("test","[test=aaa]");
        //brave.serverSpanAnnotationSubmitter().submitBinaryAnnotation();

        return String.format("_%d",uid);
    }
}
