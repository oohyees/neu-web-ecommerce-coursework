package com.example.ecommerce.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyStatusServlet extends HttpServlet {
    private final LegacyJdbcDao jdbcDao;
    private final ObjectMapper objectMapper;

    public LegacyStatusServlet(LegacyJdbcDao jdbcDao, ObjectMapper objectMapper) {
        this.jdbcDao = jdbcDao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "Servlet/JSP/Listener/Filter/JDBC legacy evidence");
        payload.put("startTime", req.getServletContext().getAttribute(LegacyAppListener.START_TIME));
        payload.put("onlineUsers", req.getServletContext().getAttribute(LegacyAppListener.ONLINE_USERS));
        payload.put("statsFromJdbc", jdbcDao.dashboardStats());
        payload.put("recentAuditLogs", LegacyAuditFilter.logs(req.getServletContext()));
        resp.setCharacterEncoding("UTF-8");
        if (req.getRequestURI().equals("/legacy/status")) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write(renderHtml(payload));
        } else {
            resp.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), payload);
        }
    }

    private String renderHtml(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) payload.get("statsFromJdbc");
        @SuppressWarnings("unchecked")
        java.util.List<String> logs = (java.util.List<String>) payload.get("recentAuditLogs");
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"><title>Legacy Web 状态页</title>")
                .append("<style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;margin:32px;background:#f8fafc;color:#111827}main{max-width:960px;margin:auto;background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:24px}table{border-collapse:collapse;width:100%;margin:16px 0 24px}th,td{border:1px solid #e5e7eb;padding:10px 12px;text-align:left}th{background:#f3f4f6;width:220px}li{margin-bottom:8px}code{background:#eef2ff;padding:2px 6px;border-radius:4px}</style>")
                .append("</head><body><main><h1>传统 Web 技术状态页</h1>")
                .append("<p>本页提供 JSP 同名源码，并在可执行 jar 环境下由 Servlet 渲染同等内容；数据来自 Listener、Filter 和原生 JDBC。</p>")
                .append("<table><tr><th>系统启动时间 Listener</th><td>").append(payload.get("startTime")).append("</td></tr>")
                .append("<tr><th>在线会话数 Listener</th><td>").append(payload.get("onlineUsers")).append("</td></tr>");
        stats.forEach((key, value) -> html.append("<tr><th>JDBC: ").append(key).append("</th><td>").append(value).append("</td></tr>"));
        html.append("</table><h2>最近访问审计 Filter</h2><ol>");
        if (logs.isEmpty()) html.append("<li>暂无审计记录，访问任意 <code>/api</code> 接口后刷新本页即可看到记录。</li>");
        else logs.forEach(log -> html.append("<li>").append(log).append("</li>"));
        html.append("</ol><p>Servlet JSON 入口：<code>/legacy/servlet/status</code></p>")
                .append("<p>JSP 源码：<code>src/main/resources/META-INF/resources/WEB-INF/jsp/legacy-status.jsp</code></p>")
                .append("</main></body></html>");
        return html.toString();
    }
}
