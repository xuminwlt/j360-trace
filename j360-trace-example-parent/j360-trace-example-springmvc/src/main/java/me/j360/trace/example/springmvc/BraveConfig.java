package me.j360.trace.example.springmvc;

import me.j360.trace.collector.core.*;
import me.j360.trace.filter.J360ServletFilter;
import me.j360.trace.http.DefaultSpanNameProvider;
import me.j360.trace.okhttp.BraveOkHttpRequestResponseInterceptor;
import me.j360.trace.spring.core.BraveApiConfig;
import me.j360.trace.springmvc.ServletHandlerInterceptor;
import okhttp3.OkHttpClient;
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
                .spanCollector(SpanCollectorForTesting.getInstance());
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

    @Autowired
    private Brave brave;
    @Autowired
    private ClientTracer clientTracer;

    @Bean
    public J360ServletFilter j360ServletFilter(){
        return  new J360ServletFilter(brave.serverRequestInterceptor(), brave.serverResponseInterceptor(), new DefaultSpanNameProvider());

    }

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient.Builder()
                .addInterceptor(new BraveOkHttpRequestResponseInterceptor(new ClientRequestInterceptor(clientTracer), new ClientResponseInterceptor(clientTracer), new DefaultSpanNameProvider()))
                .build();
    }
}
