package com.bytesfield.schedula.controllers;

import com.bytesfield.schedula.dtos.LoginDto;
import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.services.AuthService;
import com.bytesfield.schedula.services.UserService;
import com.bytesfield.schedula.utils.responses.ApiResponse;
import com.bytesfield.schedula.validations.LoginRequest;
import com.bytesfield.schedula.validations.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final UserService userService;

    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest requestBody) {
        UserDto user = userService.registerUser(requestBody);

        return ResponseEntity.ok(new ApiResponse<>("User registration successfully.", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginDto>> login(@Valid @RequestBody LoginRequest requestBody) {
        LoginDto user = authService.loginUser(requestBody);

        return ResponseEntity.ok(new ApiResponse<>("User registration successfully.", user));
    }
}
