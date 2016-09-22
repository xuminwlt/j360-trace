package me.j360.trace.example.dubbo.client;

import me.j360.trace.collector.core.Brave;
import me.j360.trace.example.dubbo.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Package: me.j360.trace.example.dubbo.client
 * User: min_xu
 * Date: 16/9/22 下午2:46
 * 说明：
 */
public class UserController {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"client.xml"});
        context.start();

        Brave brave = (Brave) context.getBean("brave");

        UserService userService = (UserService) context.getBean("userService");
        String name = userService.getUserName(1L);
        System.out.println(name);
    }

}
