package org.example.mv.web;

import org.example.mv.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ChatHistory {
    final int maxMessages = 100;
    final List<ChatMessage> chatMessageList = new ArrayList<>(100);
    final Lock readLock;
    final Lock writeLock;

    public ChatHistory(){
        var lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    public List<ChatMessage> getHistory(){
        this.readLock.lock();
        try{
            return List.copyOf(chatMessageList);
        }finally {
            this.readLock.unlock();
        }
    }

   public void addToHistory(ChatMessage message){
        this.writeLock.lock();
        try {
            this.chatMessageList.add(message);
            if(this.chatMessageList.size() > maxMessages){
                this.chatMessageList.remove(0);
            }
        }finally {
            this.writeLock.unlock();
        }
   }
}
