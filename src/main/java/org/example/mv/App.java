package org.example.mv;

import org.apache.catalina.Context;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

public class App {
    public static  void main(String[] args) throws Exception{
//        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
//        UserService userService = context.getBean(UserService.class);
//        User user = userService.register("xiaojunling@foxmail.com","12345","xjl");
//        System.out.println(user.getId());

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port",8080));
        tomcat.getConnector();
        Context ctx = tomcat.addWebapp("",new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot resourceRoot = new StandardRoot(ctx);
        resourceRoot.addPreResources(
                new DirResourceSet(resourceRoot, "/WEB-INF/classes",
                        new File("target/classes").getAbsolutePath(),"/")
        );
        ctx.setResources(resourceRoot);
        tomcat.start();
        tomcat.getServer().await();
    }
}
