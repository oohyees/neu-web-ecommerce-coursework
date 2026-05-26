<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Legacy Web 状态页</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; margin: 32px; color: #111827; background: #f8fafc; }
        main { max-width: 960px; margin: 0 auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 24px; }
        h1 { margin-top: 0; font-size: 24px; }
        table { border-collapse: collapse; width: 100%; margin: 16px 0 24px; }
        th, td { border: 1px solid #e5e7eb; padding: 10px 12px; text-align: left; }
        th { background: #f3f4f6; width: 220px; }
        code { background: #eef2ff; padding: 2px 6px; border-radius: 4px; }
        li { margin-bottom: 8px; }
    </style>
</head>
<body>
<main>
    <h1>传统 Web 技术状态页</h1>
    <p>本页由 JSP 渲染，数据来自 ServletContext、Listener、Filter 审计记录和原生 JDBC 查询。</p>
    <table>
        <tr><th>系统启动时间 Listener</th><td><%= request.getAttribute("startTime") %></td></tr>
        <tr><th>在线会话数 Listener</th><td><%= request.getAttribute("onlineUsers") %></td></tr>
        <%
            Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
        %>
        <tr><th>JDBC: <%= entry.getKey() %></th><td><%= entry.getValue() %></td></tr>
        <% } %>
    </table>
    <h2>最近访问审计 Filter</h2>
    <ol>
        <%
            List<String> logs = (List<String>) request.getAttribute("auditLogs");
            if (logs.isEmpty()) {
        %>
        <li>暂无审计记录，访问任意 <code>/api</code> 接口后刷新本页即可看到记录。</li>
        <%
            } else {
                for (String log : logs) {
        %>
        <li><%= log %></li>
        <%      }
            }
        %>
    </ol>
    <p>Servlet JSON 入口：<code>/legacy/servlet/status</code></p>
</main>
</body>
</html>
