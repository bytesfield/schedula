package com.bytesfield.schedula.validations;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotNull(message = "ConfirmPassword is required")
    @Size(min = 8, message = "ConfirmPassword must be at least 8 characters long")
    private String confirmPassword;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number format")

    private String phoneNumber;
}