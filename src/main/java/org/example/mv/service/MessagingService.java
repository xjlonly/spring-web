package org.example.mv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mv.entity.MailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Component
public class MessagingService {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JmsTemplate jmsTemplate;

    public void sendMailMessage(MailMessage msg) throws Exception{
        String text = objectMapper.writeValueAsString(msg);
        jmsTemplate.send("jms/queue/mail", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        });
    }
}
