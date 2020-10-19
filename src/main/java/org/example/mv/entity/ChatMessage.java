package org.example.mv.entity;

import java.awt.*;

public class ChatMessage extends ChatText{
    public long timestamp;
    public String name;
    public ChatMessage(){

    }

    public ChatMessage(String name, String text){
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.text = text;
    }
}


