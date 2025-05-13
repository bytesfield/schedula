package com.bytesfield.schedula.models.entities;

import com.bytesfield.schedula.models.enums.UserVerificationChannel;
import com.bytesfield.schedula.models.enums.UserVerificationStatus;
import com.bytesfield.schedula.models.enums.UserVerificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_verifications")
public class UserVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private UserVerificationType type;

    @Column(name = "channel", nullable = false, length = 50)
    private UserVerificationChannel channel;

    @Column(name = "status", nullable = false, length = 50)
    private UserVerificationStatus status;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
