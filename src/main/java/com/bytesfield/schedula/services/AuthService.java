package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.LoginDto;
import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.exceptions.InvalidCredentialsException;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.validations.LoginRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

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

        String token = jwtService.generateToken(data.getEmail());

        if (token == null) {
            throw new ConflictException("Can not login user. Try again later");
        }

        Date expirationDate = jwtService.extractExpiration(token);

        UserDto userDto = new UserDto(user);

        return new LoginDto(userDto, "Bearer", String.valueOf(expirationDate), token);
    }

    private void ensureUserCanLogin(User user, String password) {
        if (!user.isPasswordValid(password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ConflictException("User is not active");
        }
    }
}
