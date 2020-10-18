package org.example.mv.interceptor;

import org.example.mv.web.UserController;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;

@Component
public class ChatHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    public ChatHandshakeInterceptor(){
        super(List.of(UserController.USER_KEY));
    }
}
