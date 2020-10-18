package org.example.mv.filter;

import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.example.mv.web.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AuthFilter implements Filter {
    @Autowired
    UserService userService;

    final Logger logger = LoggerFactory.getLogger(getClass());
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        try{
            String authHeader = req.getHeader("Authorization");
            if(authHeader != null && authHeader.startsWith("Basic")){
                String up = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
                int pos = up.indexOf(":");
                if(pos > 0){
                    String email = URLDecoder.decode(up.substring(0,pos),StandardCharsets.UTF_8);
                    String password = URLDecoder.decode(up.substring(pos+1),StandardCharsets.UTF_8);
                    User user = userService.signin(email,password);
                    req.getSession().setAttribute(UserController.USER_KEY,user);
                }
            }
        }catch (RuntimeException e){
            logger.warn("login by authorization header failed",e);
        }

        chain.doFilter(request,response);
    }

    private String suffixFrom(String authHeader) {
        return authHeader.split(":")[0];
    }

    private String prefixFrom(String authHeader) {
        return authHeader.split(":")[1];
    }

}
