package org.example.mv.web;

import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import javax.servlet.http.HttpSession;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    static final String USER_KEY = "_user_";
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;


    @GetMapping("/")
    public ModelAndView index(HttpSession session){
        User user = (User)session.getAttribute(USER_KEY);
        Map<String,User> map = new HashMap<>();
        if(user != null && user.getId() > 0){
            map.put("user", user);
        }
        return new ModelAndView("index.html", map);
    }
    @GetMapping("/signin")
    public  ModelAndView signin(HttpSession session){
        User user = (User) session.getAttribute(USER_KEY);
        if(user != null){
            return new ModelAndView("redirect:/profile.html");
        }
        return  new ModelAndView("signin.html");
    }
    @PostMapping("/signin")
    public ModelAndView dosignin(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session){
        User user = userService.signin(email,password);
        if(user != null){
            session.setAttribute(USER_KEY, user);
            return new ModelAndView("/profile.html");
        }
        return new ModelAndView("siginin.html",Map.of("email",email,"error","login failed"));
    }
}
