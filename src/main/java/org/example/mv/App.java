package org.example.mv;

import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static  void main(String[] args){
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService userService = context.getBean(UserService.class);
        User user = userService.register("xiaojunling@foxmail.com","12345","xjl");
        System.out.println(user.getId());
    }
}
