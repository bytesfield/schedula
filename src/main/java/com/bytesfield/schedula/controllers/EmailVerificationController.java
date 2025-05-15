package com.bytesfield.schedula.controllers;

import com.bytesfield.schedula.services.UserService;
import com.bytesfield.schedula.utils.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email-verification")
public class EmailVerificationController {

    private final UserService userService;

    public EmailVerificationController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/verify/{token}")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @PathVariable String token) {
        userService.verifyEmail(token);

        return ResponseEntity.ok(new ApiResponse<>("Email verified successfully."));
    }
}
