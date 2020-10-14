package org.example.mv;

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

import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource("jdbc.properties")
public class AppConfig {

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
