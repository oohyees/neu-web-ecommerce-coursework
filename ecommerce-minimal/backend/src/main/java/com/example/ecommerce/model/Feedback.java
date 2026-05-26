package com.example.ecommerce.model;
import java.time.LocalDateTime;
public class Feedback {
    private Long id; private Long userId; private String type; private String content; private String contact; private String reply; private String status; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public String getType(){return type;} public void setType(String type){this.type=type;}
    public String getContent(){return content;} public void setContent(String content){this.content=content;}
    public String getContact(){return contact;} public void setContact(String contact){this.contact=contact;}
    public String getReply(){return reply;} public void setReply(String reply){this.reply=reply;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
