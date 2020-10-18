package org.example.mv.web;

import org.example.mv.entity.SignInRequest;
import org.example.mv.entity.User;
import org.example.mv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CrossOrigin(origins = "http://localhost:8080")//允许 CORS
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    UserService userService;

    @GetMapping("/users")
    public List<User> users(){
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable("id") long id){
        return userService.getUserById(id);
    }

    @PostMapping("/users/signin")
    public Map<String,Object> signin(@RequestBody SignInRequest signInRequest){
        try{
            User user = userService.signin(signInRequest.email,signInRequest.password);
            return Map.of("user", user);
        }catch (RuntimeException e){
            return Map.of("error", "SIGNIN_FAILED","message",e.getMessage());
        }
    }

    ///异步处理 方法一
    @GetMapping("/async/users")
    public Callable<List<User>> userList(){
        return ()->{
          try{
              Thread.sleep(3000);

          }catch (InterruptedException e){
          }
          return userService.getUsers();
        };
    }

    ///异步处理方法二
    @GetMapping("/async/users/{id}")
    public DeferredResult<User> userbyId(@PathVariable("id") long id){
        DeferredResult<User> result = new DeferredResult<>(3000L);
        new Thread(()->{
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){

            }
            try {
                User user = userService.getUserById(id);
                result.setResult(user);
            }catch (Exception e){
                result.setErrorResult(Map.of("error",e.getClass().getSimpleName(),"message",e.getMessage()));
            }
        }).start();
        return result;
    }
}



