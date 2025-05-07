package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.exceptions.InvalidCredentialsException;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.UserRole;
import com.bytesfield.schedula.repositories.UserRepository;
import com.bytesfield.schedula.utils.Helper;
import com.bytesfield.schedula.validations.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    public UserDto getUserProfile(String email) {
        User user = getUserByEmail(email);

        return new UserDto(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto registerUser(RegisterRequest data) {
        ensurePasswordMatches(data.getPassword(), data.getConfirmPassword());

        ensureUserDoesNotExist(data.getEmail(), data.getPhoneNumber());

        User user = new User();

        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setEmail(data.getEmail());
        user.setPhoneNumber(data.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setIsActive(false);
        user.setPassword(Helper.hashPassword(data.getPassword()));

        User saveduser = userRepository.save(user);

        return new UserDto(saveduser);
    }

    private void ensurePasswordMatches(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new InvalidCredentialsException("Password and confirm password do not match");
        }
    }

    private void ensureUserDoesNotExist(String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getEmail().equals(email)) {
                throw new ConflictException("A user with this email already exists");
            }

            if (user.getPhoneNumber().equals(phoneNumber)) {
                throw new ConflictException("A user with this phone number already exists");
            }
        }
    }
}
