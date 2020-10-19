package org.example.mv.web;

import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import javax.servlet.http.HttpSession;
import java.nio.channels.SeekableByteChannel;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    public  static final String USER_KEY = "__user__";
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;


    @GetMapping("")
    public ModelAndView index(HttpSession session){
        User user = (User)session.getAttribute(USER_KEY);
        Map<String,User> map = new HashMap<>();
        if(user != null && user.getId() > 0){
            map.put("user", user);
        }
        return new ModelAndView("index.html", map);
    }

    @GetMapping("/register")
    public ModelAndView register(){
        return new ModelAndView("register.html");
    }

    @PostMapping("/register")
    public ModelAndView doRegister(@RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("name") String name){
        try {
            User user = userService.register(email, password, name);
            logger.info("user registered:{}", user.getEmail());
        }catch (RuntimeException e){
            return new ModelAndView("register.html",Map.of("email", email, "error", "Register failed"));
        }
        return  new ModelAndView("redirect:/user/signin");

    }

    @GetMapping("/signin")
    public  ModelAndView signin(HttpSession session){
        User user = (User) session.getAttribute(USER_KEY);
        if(user != null){
            return new ModelAndView("redirect:/user/profile");
        }
        return  new ModelAndView("signin.html");
    }
    @PostMapping("/signin")
    public ModelAndView dosignin(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session){
        try{
            User user = userService.signin(email,password);
            if(user != null){
                session.setAttribute(USER_KEY, user);
            }
        }catch (RuntimeException e){
            return new ModelAndView("signin.html",Map.of("email", email,"error","login failed"));
        }
        return new ModelAndView("redirect:/user/profile");
    }
    @GetMapping("/profile")
    public  ModelAndView profile(HttpSession session){
        User user = (User) session.getAttribute(USER_KEY);
        if(user == null){
            return  new ModelAndView("redirect:/user/signin");
        }
        return new ModelAndView("profile.html",Map.of("user",user));
    }

    @GetMapping("/signout")
    public String signout(HttpSession session){
        session.removeAttribute(USER_KEY);
        return "redirect:/user/signin";
    }

    @GetMapping("/modifypassword")
    public ModelAndView modifypassword(){
        return new ModelAndView("password.html");
    }
    @PostMapping("/modifypassword")
    public ModelAndView domodifypassword(@RequestParam("password") String password,
                        @RequestParam("newpassword") String newpassword,
                        HttpSession session){
        User user = (User) session.getAttribute(USER_KEY);
        if(user == null){
            return new ModelAndView("redirect:/user/signin");
        }
        if(user.getPassword().equals(password)){
            user.setPassword(newpassword);
            userService.updateUser(user);
            session.removeAttribute(USER_KEY);
            return new ModelAndView("redirect:/user/profile");
        }
        return new ModelAndView("password.html", Map.of("error", "old password error"));
    }


    ///REST API
    @PostMapping(value = "/rest",
            consumes = "application/json;charset=utf-8",
            produces = "application/json;charset=utf-8")
    @ResponseBody
    public String rest(@RequestParam User user){
        return "{\"restSupport\":true}";
    }


    //Exception Handle 仅在当前Controller中生效 多个Controller共用可以放在aseController中 或使用ControllerAdvice
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnknowException(Exception exception){
        return new ModelAndView("500.html",Map.of("error",exception.getClass(),"message",exception.getMessage()));
    }
}
