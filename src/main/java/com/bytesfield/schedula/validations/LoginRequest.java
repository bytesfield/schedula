package com.bytesfield.schedula.validations;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Password is required")
    private String password;
}