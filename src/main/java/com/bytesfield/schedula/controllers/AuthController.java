package com.bytesfield.schedula.controllers;

import com.bytesfield.schedula.dtos.LoginDto;
import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.services.AuthService;
import com.bytesfield.schedula.services.UserService;
import com.bytesfield.schedula.utils.responses.ApiResponse;
import com.bytesfield.schedula.validations.LoginRequest;
import com.bytesfield.schedula.validations.RefreshTokenRequest;
import com.bytesfield.schedula.validations.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        authService.logoutUser(userDetails, request);

        return ResponseEntity.ok(new ApiResponse<>("Logout successfully."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginDto>> login(@Valid @RequestBody RefreshTokenRequest requestBody, @AuthenticationPrincipal UserDetails userDetails) {
        LoginDto response = authService.refreshToken(userDetails, requestBody.getToken());

        return ResponseEntity.ok(new ApiResponse<>("Token refreshed successfully.", response));
    }
}
