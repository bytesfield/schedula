package com.bytesfield.schedula.validations;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class VerifyEmailRequest {
    @NotBlank(message = "Token is required")
    private String token;
}