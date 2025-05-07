package com.bytesfield.schedula.repositories;

import com.bytesfield.schedula.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

}
