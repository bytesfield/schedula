package com.bytesfield.schedula.validations;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh Token is required")
    private String token;
}