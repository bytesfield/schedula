package com.bytesfield.schedula.services;

import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.exceptions.InvalidCredentialsException;
import com.bytesfield.schedula.exceptions.UserNotFoundException;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.entities.UserVerification;
import com.bytesfield.schedula.models.enums.UserRole;
import com.bytesfield.schedula.models.enums.UserVerificationChannel;
import com.bytesfield.schedula.models.enums.UserVerificationStatus;
import com.bytesfield.schedula.models.enums.UserVerificationType;
import com.bytesfield.schedula.producers.UserRegisteredProducer;
import com.bytesfield.schedula.repositories.UserRepository;
import com.bytesfield.schedula.repositories.UserVerificationRepository;
import com.bytesfield.schedula.services.utils.CacheService;
import com.bytesfield.schedula.utils.Helper;
import com.bytesfield.schedula.utils.security.EncryptionUtil;
import com.bytesfield.schedula.validations.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final UserRegisteredProducer userRegisteredProducer;
    private final CacheService cacheService;

    @Value("${email.verification.expiry-in-seconds:300}")
    private String emailVerificationExpiryInSeconds;

    public UserDto getUserProfile(String email) {
        try {
            User user = getUserByEmail(email);

            return new UserDto(user);
        } catch (Exception e) {
            if (e instanceof ConflictException || e instanceof UserNotFoundException) {
                throw e;
            }

            log.error("Error while getting user profile: {}", e.getMessage(), e);

            throw new ServerErrorException("Something went wrong. You can reach out to us", e);
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public UserDto registerUser(RegisterRequest data) {
        try {
            ensurePasswordMatches(data.getPassword(), data.getConfirmPassword());

            ensureUserDoesNotExist(data.getEmail(), data.getPhoneNumber());

            UserDto userDto = createUserAndRelatedRecords(data);

            userRegisteredProducer.sendUserRegistered(userDto); // Publish user registration event

            return userDto;
        } catch (Exception e) {
            if (e instanceof ConflictException || e instanceof UserNotFoundException) {
                throw e;
            }

            log.error("Error while registering user: {}", e.getMessage(), e);

            throw new ServerErrorException("Something went wrong. You can reach out to us", e);
        }
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

    private UserDto createUserAndRelatedRecords(RegisterRequest data) {
        String verificationCode = Helper.generateUniqueCharacters(32);
        User user = new User();

        String encryptedVerificationCode = EncryptionUtil.encrypt(verificationCode);

        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setEmail(data.getEmail());
        user.setPhoneNumber(data.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setIsActive(false);
        user.setEmailVerificationToken(encryptedVerificationCode);
        user.setPassword(Helper.hashPassword(data.getPassword()));

        User savedUser = userRepository.save(user);

        UserVerification emailVerification = new UserVerification();

        emailVerification.setUser(savedUser);
        emailVerification.setType(UserVerificationType.EMAIL);
        emailVerification.setChannel(UserVerificationChannel.EMAIL);
        emailVerification.setStatus(UserVerificationStatus.PENDING);

        userVerificationRepository.save(emailVerification);

        // Cache the verification token for later use
        cacheUserVerificationToken(data.getEmail(), encryptedVerificationCode);

        return new UserDto(savedUser);
    }

    private void cacheUserVerificationToken(String email, String verificationCode) {
        int expiryInSeconds = Integer.parseInt(emailVerificationExpiryInSeconds);

        cacheService.setValueWithExpiration(this.getVerificationCacheKey(email), verificationCode, expiryInSeconds, TimeUnit.SECONDS);
    }

    private String getVerificationCacheKey(String email) {
        return "verification-token:" + email;
    }

    @Transactional
    public void verifyEmail(String token) {
        try {
            String encryptedToken = EncryptionUtil.encrypt(token);

            User user = this.getUserByEmailToken(encryptedToken);

            if (user == null) {
                throw new ConflictException("Invalid or expired token");
            }

            String cachedToken = getUserVerificationTokenFromCache(user.getEmail());

            if (!cachedToken.equals(encryptedToken)) {
                throw new ConflictException("Invalid or expired token");
            }

            this.makeUserAsVerified(user);

            cacheService.deleteValue(this.getVerificationCacheKey(user.getEmail()));
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while verifying user email: {}", e.getMessage(), e);
            throw new ServerErrorException("Something went wrong. You can reach out to us", e);
        }
    }

    public User getUserByEmailToken(String token) {
        return userRepository.findByEmailVerificationToken(token).orElse(null);
    }

    private String getUserVerificationTokenFromCache(String email) {
        String verificationCode = cacheService.getValue(this.getVerificationCacheKey(email));

        if (verificationCode == null) {
            throw new ConflictException("Invalid or expired token");
        }

        return verificationCode;
    }

    private void makeUserAsVerified(User user) {
        user.setIsActive(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        UserVerification userVerification = userVerificationRepository.findUserVerification(user, UserVerificationType.EMAIL, UserVerificationChannel.EMAIL);

        userVerification.setVerifiedAt(Instant.now());
        userVerification.setStatus(UserVerificationStatus.COMPLETED);
        userVerificationRepository.save(userVerification);
    }
}
