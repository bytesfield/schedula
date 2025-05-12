package com.bytesfield.schedula.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        String error = "error";

        switch (authException) {
            case BadCredentialsException badCredentialsException -> body.put(error, "Invalid username or password");
            case LockedException lockedException -> body.put(error, "Account is locked");
            case DisabledException disabledException -> body.put(error, "Account is disabled");
            case AccountExpiredException accountExpiredException -> body.put(error, "Account has expired");
            case CredentialsExpiredException credentialsExpiredException -> body.put(error, "Credentials have expired");
            case null, default -> body.put(error, "Unauthorized");
        }

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
