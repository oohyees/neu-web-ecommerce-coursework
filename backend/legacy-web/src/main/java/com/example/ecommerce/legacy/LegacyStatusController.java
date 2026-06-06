package com.example.ecommerce.legacy;

import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyStatusController {
    private final LegacyJdbcDao jdbcDao;
    private final ServletContext servletContext;

    public LegacyStatusController(LegacyJdbcDao jdbcDao, ServletContext servletContext) {
        this.jdbcDao = jdbcDao;
        this.servletContext = servletContext;
    }

    @GetMapping("/legacy/status")
    public String status(Model model) {
        model.addAttribute("startTime", servletContext.getAttribute(LegacyAppListener.START_TIME));
        model.addAttribute("onlineUsers", servletContext.getAttribute(LegacyAppListener.ONLINE_USERS));
        model.addAttribute("stats", jdbcDao.dashboardStats());
        model.addAttribute("auditLogs", LegacyAuditFilter.logs(servletContext));
        return "legacy-status";
    }
}
