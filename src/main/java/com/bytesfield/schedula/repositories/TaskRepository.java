package com.bytesfield.schedula.repositories;

import com.bytesfield.schedula.models.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("SELECT t FROM Task t WHERE t.nextRunAt <= :now AND t.status = 'PENDING'")
    List<Task> findDueTasks(Instant now);

    @Modifying
    @Query("UPDATE Task t SET t.status = 'QUEUED' WHERE t.id = :taskId")
    void markAsQueued(Integer taskId);
}
