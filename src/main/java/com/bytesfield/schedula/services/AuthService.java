package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.LoginDto;
import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.exceptions.InvalidCredentialsException;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.JwtTokenType;
import com.bytesfield.schedula.services.utils.JwtService;
import com.bytesfield.schedula.validations.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;

    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public LoginDto loginUser(LoginRequest data) {
        User user = userService.getUserByEmail(data.getEmail());

        ensureUserCanLogin(user, data.getPassword());

        return this.login(user);
    }

    private void ensureUserCanLogin(User user, String password) {
        if (!user.isPasswordValid(password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ConflictException("User is not active");
        }
    }

    private LoginDto login(User user) {
        Map<String, Object> tokens = jwtService.generateTokens(user.getEmail());

        if (tokens.isEmpty()) {
            throw new ConflictException("Can not login user. Try again later");
        }

        String accessToken = (String) tokens.get("accessToken");
        String refreshToken = (String) tokens.get("refreshToken");

        Date expirationDate = jwtService.extractExpiration(accessToken, JwtTokenType.ACCESS);

        UserDto userDto = new UserDto(user);

        LoginDto loginData = new LoginDto(userDto, "Bearer", String.valueOf(expirationDate), accessToken, refreshToken);

        jwtService.removeAccessTokenInvalidation(accessToken);

        jwtService.cacheAccessToken(user, accessToken);

        return loginData;
    }

    public void logoutUser(UserDetails userDetails, HttpServletRequest request) {
        User user = this.getUser(userDetails);

        String token = this.extractTokenFromRequest(request);

        jwtService.invalidateAccessToken(token);

        jwtService.deleteCachedAccessToken(user.getEmail());
    }

    private User getUser(UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());

        if (user == null) {
            throw new ConflictException("User not found");
        }

        return user;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public LoginDto refreshToken(UserDetails userDetails, String refreshToken) {
        User user = this.getUser(userDetails);

        if (!jwtService.isTokenValid(refreshToken, JwtTokenType.REFRESH, userDetails)) {
            throw new ConflictException("Invalid refresh token");
        }

        return this.login(user);
    }
}
