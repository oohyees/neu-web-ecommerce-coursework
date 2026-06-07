package com.example.ecommerce.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class SupportController {
    private final JdbcTemplate jdbc;

    public SupportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/feedback")
    public ApiResponse<?> myFeedback(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,type,content,contact,reply,status,created_at createdAt
                from feedback where user_id=? order by id desc
                """, userId));
    }

    @PostMapping("/feedback")
    public ApiResponse<?> createFeedback(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into feedback(user_id,type,content,contact,status,created_at)
                values(?,?,?,?,?,?)
                """, userId, stringValue(body.get("type")), stringValue(body.get("content")),
                stringValue(body.get("contact")), "PENDING", LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,type,content,contact,reply,status,created_at createdAt
                from feedback where id=?
                """, id));
    }

    @GetMapping("/consultations")
    public ApiResponse<?> myConsultations(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where user_id=? order by id desc
                """, userId));
    }

    @PostMapping("/consultations")
    public ApiResponse<?> createConsultation(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into customer_consultation(user_id,subject,content,status,created_at)
                values(?,?,?,?,?)
                """, userId, stringValue(body.get("subject")), stringValue(body.get("content")), "PENDING", LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where id=?
                """, id));
    }

    @GetMapping("/consultations/chat")
    public ApiResponse<?> chatHistory(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where user_id=? and subject='在线对话'
                order by id asc
                """, userId));
    }

    @PostMapping("/consultations/chat")
    public ApiResponse<?> sendChat(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        String content = stringValue(body.get("content"));
        jdbc.update("""
                insert into customer_consultation(user_id,subject,content,status,created_at)
                values(?,?,?,'PENDING',?)
                """, userId, "在线对话", content, LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);

        // Auto-reply
        String reply = autoReply(content);
        jdbc.update("update customer_consultation set reply=?, status='REPLIED' where id=?", reply, id);

        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where id=?
                """, id));
    }

    private String autoReply(String userMessage) {
        String msg = userMessage.toLowerCase();
        if (msg.contains("退款") || msg.contains("退货")) return "关于退款/退货问题，请在订单详情中申请退款，我们会在1-3个工作日内处理。";
        if (msg.contains("发货") || msg.contains("物流") || msg.contains("快递")) return "发货后您可以在订单详情中查看物流信息，一般1-3天到达。";
        if (msg.contains("支付") || msg.contains("付款")) return "我们支持模拟支付，下单后请在30分钟内完成支付，超时订单将自动取消。";
        if (msg.contains("优惠") || msg.contains("折扣") || msg.contains("券")) return "您可以在首页领取优惠券，结算时选择使用。关注活动公告获取更多优惠信息！";
        if (msg.contains("密码") || msg.contains("账号")) return "如忘记密码，可在登录页点击「找回密码」，通过邮箱验证码重置。";
        if (msg.contains("你好") || msg.contains("在吗") || msg.contains("hi") || msg.contains("hello")) return "您好！欢迎咨询，请问有什么可以帮您？";
        return "感谢您的咨询！客服正在处理中，请稍候。您也可以在「我的订单」中查看订单状态。";
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
