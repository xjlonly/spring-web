package org.example.mv.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mv.entity.ChatMessage;
import org.example.mv.entity.ChatText;
import org.example.mv.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ChatHistory chatHistory;

    @Autowired
    ObjectMapper objectMapper;

    //保存所有websocket的Client会话实例
    private Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    public void broadcastMessage(ChatMessage chatMessage) throws IOException{
        TextMessage message = toTextMessage(List.of(chatMessage));
        for(String id : clients.keySet()){
            WebSocketSession session = clients.get(id);
            session.sendMessage(message);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String s = message.getPayload().strip();
        if(s.isEmpty()){
            return;
        }
        String name = (String)session.getAttributes().get("name");
        ChatText chatText = objectMapper.readValue(s, ChatText.class);
        var msg = new ChatMessage(name, chatText.text);
        chatHistory.addToHistory(msg);
        broadcastMessage(msg);
    }

    private TextMessage toTextMessage(List<ChatMessage> messages) throws IOException{
        String json = objectMapper.writeValueAsString(messages);
        return new TextMessage(json);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        clients.put(session.getId(),session);
        String name = null;
        User user = (User)session.getAttributes().get("__user__");
        if(user != null){
            name = user.getName();
        }else{
            name = initGuestName();
        }
        session.getAttributes().put("name",name);
        logger.info("websocket connection established:id={}, name={}",session.getId(),name);

        List<ChatMessage> list = chatHistory.getHistory();
        session.sendMessage(toTextMessage(list));

        var msg = new ChatMessage("SYSTEM MESSAGE",name+"joined the room.");
        chatHistory.addToHistory(msg);
        broadcastMessage(msg);
    }

    private String initGuestName(){
        return "Guest" + this.guestNumber.incrementAndGet();
    }

    private AtomicInteger guestNumber = new AtomicInteger();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clients.remove(session.getId());
        logger.info("websocket connection closed:id={},close-status={}",session.getId(),status);
    }
}
