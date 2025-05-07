package com.bytesfield.schedula.repositories;

import com.bytesfield.schedula.models.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
