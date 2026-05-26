package com.example.ecommerce.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class GatewayController {
    private final RestTemplate restTemplate;
    @Value("${services.auth}") private String authService;
    @Value("${services.product}") private String productService;
    @Value("${services.order}") private String orderService;
    @Value("${services.admin}") private String adminService;

    public GatewayController(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    @RequestMapping(value = "/auth/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> auth(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(authService, request, body, "/api/auth", "/auth");
    }

    @RequestMapping(value = "/products/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> product(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(productService, request, body, "/api/products", "/products");
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public ResponseEntity<String> categories(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(productService, request, body, "/api/categories", "/products/categories");
    }

    @RequestMapping(value = {"/orders/**", "/cart/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> order(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(orderService, request, body, "/api", "");
    }

    @RequestMapping(value = "/admin/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> admin(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(adminService, request, body, "/api/admin", "/admin");
    }

    private ResponseEntity<String> forward(String service, HttpServletRequest request, String body, String fromPrefix, String toPrefix) {
        String path = request.getRequestURI().replaceFirst(fromPrefix, toPrefix);
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase("host"))
                .filter(name -> !name.equalsIgnoreCase("content-length"))
                .filter(name -> !name.equalsIgnoreCase("transfer-encoding"))
                .forEach(name -> headers.add(name, request.getHeader(name)));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> downstream = restTemplate.exchange(service + path + query, HttpMethod.valueOf(request.getMethod()), entity, String.class);
        HttpHeaders responseHeaders = new HttpHeaders();
        if (downstream.getHeaders().getContentType() != null) responseHeaders.setContentType(downstream.getHeaders().getContentType());
        return new ResponseEntity<>(downstream.getBody(), responseHeaders, downstream.getStatusCode());
    }
}
