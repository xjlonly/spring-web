package org.example.mv;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.tomcat.util.descriptor.LocalResolver;
import org.example.mv.interceptor.ChatHandshakeInterceptor;
import org.example.mv.web.ChatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.*;

@Configuration
@ComponentScan
@EnableScheduling
@EnableWebSocket
@EnableWebMvc
@EnableMBeanExport //自动注册Bean
@EnableTransactionManagement
@EnableJms
@PropertySource({"classpath:/jdbc.properties","classpath:/jms.properties", "classpath:/task.properties"})
public class AppConfig {


    @Bean
    WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors){
        return  new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }

            //启用拦截器
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                for(var interceptor : interceptors){
                    registry.addInterceptor(interceptor);
                }
            }

            //使用全局的CORS配置  也可以使用CORSFilter需配置web.xml比较繁琐
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET","POST")
                        .maxAge(3600);
            }
        };
    }



    @Bean
    LocaleResolver createLocalResolver(){
        var clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.ENGLISH);
        clr.setDefaultTimeZone(TimeZone.getDefault());
        return clr;
    }
    @Bean("i18n")
    MessageSource createMessageSource(){
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    WebSocketConfigurer createWebSocketConfigurer(@Autowired ChatHandler chatHandler,
                                                  @Autowired ChatHandshakeInterceptor chatHandshakeInterceptor){
        return webSocketHandlerRegistry -> {
            //把URL与指定的WebSocketHandler关联，可关联多个
            webSocketHandlerRegistry.addHandler(chatHandler,"/chat").addInterceptors(chatHandshakeInterceptor);
        };
    }

    @Bean
    ObjectMapper createObjectMapper(){
        var om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return om;
    }

    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext,
                                    @Autowired @Qualifier("i18n") MessageSource messageSource){
        PebbleEngine engine = new PebbleEngine.Builder()
                .autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(createExtension(messageSource))
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return  viewResolver;
    }

    private Extension createExtension(MessageSource messageSource){
        return  new AbstractExtension() {
            @Override
            public Map<String, Function> getFunctions() {
                return Map.of("_", new Function() {
                    @Override
                    public Object execute(Map<String, Object> map, PebbleTemplate pebbleTemplate, EvaluationContext evaluationContext, int i) {
                        String key = (String) map.get("0");
                        List<Object> arguments = this.extractArguments(map);
                        Locale locale = (Locale)evaluationContext.getVariable("__local__");
                        return messageSource.getMessage(key,arguments.toArray(),"???" + key + "???", locale);
                    }

                    private List<Object> extractArguments(Map<String, Object> args){
                        int i =1;
                        List<Object> arguments = new ArrayList<>();
                        while (args.containsKey(String.valueOf(i))){
                            Object param = args.get(String.valueOf(i));
                            arguments.add(param);
                            i++;
                        }
                        return arguments;
                    }

                    @Override
                    public List<String> getArgumentNames() {
                        return null;
                    }
                });
            }
        };
    }

    //Mq---------------------------------------------------------------------
    @Bean
    ConnectionFactory createJMSConnectionFactory(@Value("${jms.uri:tcp://localhost:61616}") String uri,
                                                 @Value("${jms.username:admin}") String username,
                                                 @Value("${jms.password:123456123456}") String password){
        return  new ActiveMQConnectionFactory(uri,username,password);
    }


    @Bean
    JmsTemplate createJmsTemplate(@Autowired ConnectionFactory connectionFactory){
        return new JmsTemplate(connectionFactory);
    }

    @Bean("jmsListenerContainerFactory")
    DefaultJmsListenerContainerFactory createDefaultJmsListenerContainerFactory(@Autowired ConnectionFactory connectionFactory){
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    //因websocket与Scheduler注解冲突 才需要创建此Bean
    @Bean
    TaskScheduler createTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(20);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    //jdbc-------------------
    @Value("${jdbc.url}")
    String jdbcUrl;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;

    @Bean
    DataSource createDateSource(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("autoCommit","false");
        config.addDataSourceProperty("connectionTimeout","5");
        config.addDataSourceProperty("idleTimeout","60");
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource){
        return  new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager createPlatformTransactionManager(@Autowired DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

}
