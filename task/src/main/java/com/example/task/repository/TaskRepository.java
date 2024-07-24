package com.example.task.repository;

import com.example.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findTaskById(Long id);
    List<Task> findByEngineerId(Long engineerId);
    List<Task> findByEngineerIdAndCreatedDate(Long engineerId, LocalDate toLocalDate);
}
