package com.example.ecommerce.common;

import java.io.Serializable;

public record SessionInfo(Long id, String role) implements Serializable {
}
