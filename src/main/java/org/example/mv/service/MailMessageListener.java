package org.example.mv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.w3c.dom.Text;

import javax.jms.Message;
import javax.jms.TextMessage;

@Component
public class MailMessageListener {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ObjectMapper objectMapper;

    @JmsListener(destination = "jms/queue/mail",concurrency = "10")
    public  void onMailMessageReceive(Message message) throws Exception{
        logger.info("received message:" + message);
        if(message instanceof TextMessage){
            String text = ((TextMessage)message).getText();
            logger.info("handle message:{}", text);
        }else{
            logger.error("unable to process non-text message!");
        }
    }
}
