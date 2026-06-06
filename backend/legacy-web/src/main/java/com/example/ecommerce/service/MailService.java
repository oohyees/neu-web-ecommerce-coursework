package com.example.ecommerce.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class MailService {
  private final JavaMailSender sender;
  @Value("${app.mail.from:}") private String from;
  public MailService(JavaMailSender sender){this.sender=sender;}
  public void sendVerificationCode(String email,String code,String purpose){
    if(from==null || from.isBlank()) throw new IllegalStateException("邮件服务未配置");
    SimpleMailMessage message=new SimpleMailMessage();
    message.setFrom(from); message.setTo(email); message.setSubject("电商平台验证码");
    message.setText(("RESET".equals(purpose)?"找回密码":"注册")+"验证码："+code+"，10 分钟内有效。");
    sender.send(message);
  }
}
