package com.bytesfield.schedula.controllers;

import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.services.UserService;
import com.bytesfield.schedula.utils.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        UserDto user = userService.getUserProfile(username);

        return ResponseEntity.ok(new ApiResponse<>("User profile retrieved successfully.", user));
    }
}
