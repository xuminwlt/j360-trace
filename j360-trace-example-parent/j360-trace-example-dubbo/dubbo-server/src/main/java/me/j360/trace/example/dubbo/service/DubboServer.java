package me.j360.trace.example.dubbo.service;

import me.j360.trace.collector.core.Brave;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

/**
 * Package: me.j360.trace.example.dubbo.service
 * User: min_xu
 * Date: 16/9/22 下午2:50
 * 说明：
 */
public class DubboServer {


    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"server.xml"});
        context.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                context.close();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

}
