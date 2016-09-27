package me.j360.trace.example.dubbo.service;

import me.j360.trace.collector.core.Brave;
import me.j360.trace.collector.core.EmptySpanCollectorMetricsHandler;
import me.j360.trace.collector.kafka.KafkaSpanCollector;
import me.j360.trace.spring.core.BraveApiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Package: me.j360.trace.example.dubbo.service
 * User: min_xu
 * Date: 16/9/22 下午3:27
 * 说明：
 */
@Configuration
@Import(BraveApiConfig.class)
public class BraveConfig {

    /*@Bean
    public Brave brave() {
        Brave.Builder builder = new Brave.Builder("J360DubboInterceptorIntegration")
                .spanCollector(SpanCollectorForTesting.getInstance());
        return builder.build();
    }*/


    @Bean
    public Brave brave() {
        Brave.Builder builder = new Brave.Builder("J360DubboInterceptorIntegration")
                .spanCollector(KafkaSpanCollector.create("172.16.10.125:9092", new EmptySpanCollectorMetricsHandler()));
        return builder.build();
    }

}
