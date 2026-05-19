package com.example.ecommerce.model;
import java.time.LocalDateTime;
public class Feedback {
    private Long id; private Long userId; private String content; private String reply; private String status; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public String getContent(){return content;} public void setContent(String content){this.content=content;}
    public String getReply(){return reply;} public void setReply(String reply){this.reply=reply;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
