package com.bytesfield.schedula.repositories;

import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.entities.UserVerification;
import com.bytesfield.schedula.models.enums.UserVerificationChannel;
import com.bytesfield.schedula.models.enums.UserVerificationType;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface UserVerificationRepository extends JpaRepository<UserVerification, Integer> {
    @Query("SELECT uv FROM UserVerification uv JOIN FETCH uv.user WHERE uv.id = :id")
    UserVerification findByIdWithUser(@Param("id") int id);

    @Query("SELECT uv FROM UserVerification uv WHERE uv.user = :user")
    List<UserVerification> findAllUserVerifications(User user);

    @Query("SELECT uv FROM UserVerification uv WHERE uv.user = :user AND uv.type = :type AND uv.channel = :channel")
    UserVerification findUserVerification(User user, UserVerificationType type, UserVerificationChannel channel);

}
