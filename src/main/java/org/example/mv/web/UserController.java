package org.example.mv.web;

import org.example.mv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
    @Autowired
    UserService userService;


    @GetMapping("/")
    public ModelAndView index(){
        return new ModelAndView();
    }
}
