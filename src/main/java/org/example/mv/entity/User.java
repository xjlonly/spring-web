package org.example.mv.entity;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class User {
    private Long id;
    private String email;
    private String name;
    private String password;
    private long createAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public String getCreateDateTime(){
        return Instant.ofEpochMilli(this.createAt).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getImageUrl(){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(this.email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return "http://www.gravatar.com/avatar/" + String.format("%032x",new BigInteger(1,hash));
        } catch (NoSuchAlgorithmException e) {
            throw  new RuntimeException();
        }
    }

    public String toString(){
        return String.format("User[id=%s, email=%s,name=%s, password=%s,createAt=%s,createdDateTime=%s]",
                getId(),getEmail(),getName(),getPassword(),getCreateAt(),getCreateDateTime());
    }
}
