package org.example.mv.service;

import com.sun.jdi.VMOutOfMemoryException;
import org.example.mv.entity.MailMessage;
import org.example.mv.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.net.PasswordAuthentication;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Component
public class UserService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MessagingService messagingService;

    RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public User getUserByEmail(String email){
        return jdbcTemplate.queryForObject("select * from users where email=?", new Object[]{email}, userRowMapper);
    }

    public User getUserById(long id){
        return jdbcTemplate.queryForObject("select * from users where id=?", new Object[]{id}, userRowMapper);
    }

    public List<User> getUsers(){
        return jdbcTemplate.query("select * from users", userRowMapper);
    }

    public User signin(String email, String password){
        logger.info("try login by {}...", email);
        User user = getUserByEmail(email);
        if(user.getPassword().equals(password)){
            return user;
        }
        throw new RuntimeException("login failed");
    }

    public User register(String email, String password, String name){
        logger.info("try register by {}...", email);
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());
        KeyHolder holder = new GeneratedKeyHolder();

        if(1 != jdbcTemplate.update(
                connection -> {
                    var ps = connection.prepareStatement(
                            "Insert into users(email,name,password,createdAt) values (?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setObject(1,email);
                    ps.setObject(2,name);
                    ps.setObject(3,password);
                    ps.setObject(4,user.getCreatedAt());
                    return ps;
                }, holder)) {
            throw new RuntimeException(" insert failed");
        }
        user.setId(Objects.requireNonNull(holder.getKey()).longValue());
        try {
            messagingService.sendMailMessage(new MailMessage(user.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public void updateUser(User user){
        if(1 != jdbcTemplate.update("update users set name=?,password=? where id=?",user.getName(),user.getPassword(),user.getId())){
            throw new RuntimeException("User not found by id");
        }
    }
}
