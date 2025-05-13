package com.bytesfield.schedula.dtos;

import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.UserRole;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private Boolean isActive;
    private Instant createdAt;

    public UserDto(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
    }
}
