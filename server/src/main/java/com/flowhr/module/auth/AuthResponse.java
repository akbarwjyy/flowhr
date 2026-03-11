package com.flowhr.module.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private List<String> roles;
}
