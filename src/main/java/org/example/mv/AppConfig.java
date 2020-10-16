package org.example.mv;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:/jdbc.properties")
public class AppConfig {


    @Bean
    WebMvcConfigurer createWebMvcConfigurer(){
        return  new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }
        };
    }

    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext){
        PebbleEngine engine = new PebbleEngine.Builder()
                .autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(new SpringExtension())
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return  viewResolver;
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
