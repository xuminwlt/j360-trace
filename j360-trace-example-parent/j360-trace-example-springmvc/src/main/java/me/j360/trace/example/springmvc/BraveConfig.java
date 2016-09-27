package me.j360.trace.example.springmvc;

import me.j360.trace.collector.core.*;
import me.j360.trace.collector.kafka.KafkaSpanCollector;
import me.j360.trace.http.DefaultSpanNameProvider;
import me.j360.trace.spring.core.BraveApiConfig;
import me.j360.trace.springmvc.ServletHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Import(BraveApiConfig.class)
@EnableWebMvc
public class BraveConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private ServerRequestInterceptor requestInterceptor;

    @Autowired
    private ServerResponseInterceptor responseInterceptor;

    @Autowired
    private ServerSpanThreadBinder serverThreadBinder;

    @Bean
    public Brave brave() {
        Brave.Builder builder = new Brave.Builder("J360ServletInterceptorIntegration")
                .spanCollector(KafkaSpanCollector.create("172.16.10.125:9092", new EmptySpanCollectorMetricsHandler()));
        return builder.build();
    }

    @Bean
    public PingController pingController() {
        return new PingController();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ServletHandlerInterceptor(requestInterceptor, responseInterceptor, new DefaultSpanNameProvider(), serverThreadBinder));
    }

}
