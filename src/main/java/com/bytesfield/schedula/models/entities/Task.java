package com.bytesfield.schedula.models.entities;

import com.bytesfield.schedula.models.enums.ScheduleType;
import com.bytesfield.schedula.models.enums.TaskStatus;
import com.bytesfield.schedula.models.enums.TaskType;
import com.bytesfield.schedula.utils.converter.JsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TaskType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cron_expression", nullable = false, length = 255)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 50)
    private ScheduleType scheduleType;

    @Convert(converter = JsonConverter.class)
    @Column(name = "payload", columnDefinition = "TEXT")
    private Map<String, Object> data;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
