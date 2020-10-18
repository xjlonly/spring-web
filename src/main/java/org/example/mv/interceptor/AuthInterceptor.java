package org.example.mv.interceptor;

import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.example.mv.web.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Order(2)
@Component
public class AuthInterceptor implements HandlerInterceptor {
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try{
            logger.info("try authorization by authorization header ...");
            String authHeader = request.getHeader("Authorization");
            if(authHeader != null && authHeader.startsWith("Basic")){
                String up = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
                int pos = up.indexOf(":");
                if(pos > 0){
                    String email = URLDecoder.decode(up.substring(0,pos),StandardCharsets.UTF_8);
                    String password = URLDecoder.decode(up.substring(pos+1),StandardCharsets.UTF_8);
                    User user = userService.signin(email,password);
                    request.getSession().setAttribute(UserController.USER_KEY,user);
                }
            }
        }catch (RuntimeException e){
            logger.error("login by authorization header failed",e);
        }
        return true;
    }
}
