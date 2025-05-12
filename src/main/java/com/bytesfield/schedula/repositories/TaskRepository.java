package com.bytesfield.schedula.repositories;

import com.bytesfield.schedula.models.entities.Task;
import com.bytesfield.schedula.models.entities.User;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("SELECT t FROM Task t WHERE t.nextRunAt <= :now AND t.status IN ('PENDING', 'PROCESSING')")
    List<Task> findDueTasks(Instant now);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.id = :id")
    Task findUserTaskById(User user, int id);

    @Query("SELECT t FROM Task t WHERE t.user = :user ORDER BY t.createdAt ASC")
    List<Task> findUserTasks(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 'QUEUED' WHERE t.id = :taskId")
    void markAsQueued(Integer taskId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 'FAILED' WHERE t.id = :taskId")
    void markAsFailed(Integer taskId);

    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE t.id = :id")
    Task findByIdWithUser(@Param("id") int id);
}
