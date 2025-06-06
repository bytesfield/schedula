package com.bytesfield.schedula.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@AllArgsConstructor
@Getter
@Setter
public class LoginDto {
    private String tokenType;
    private String expiresAt;
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    public LoginDto(UserDto user, String tokenType, String expiresAt, String accessToken, String refreshToken) {
        this.user = user;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
