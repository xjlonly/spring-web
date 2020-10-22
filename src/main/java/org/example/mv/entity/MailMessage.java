package org.example.mv.entity;


public class MailMessage {
    public  String text;

    public MailMessage(){
    }

    public MailMessage(String message){
        this.text = message;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
