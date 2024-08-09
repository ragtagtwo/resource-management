package com.example.task.repository;

import com.example.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findTaskById(Long id);
    List<Task> findByEngineerId(Long engineerId);
    List<Task> findByEngineerIdAndCreatedDate(Long engineerId, LocalDate toLocalDate);



    List<Task> findByNameAndCreatedDateAndShift(String chat, LocalDate localDate, String shift);

    List<Task> findByNameAndCreatedDate(String p1, LocalDate localDate);



    List<Task> findByEngineerIdAndCreatedDateAndNameIn(Long id, LocalDate localDate, List<String> p1);

    List<Task> findByEngineerIdAndCreatedDateAndName(Long id, LocalDate localDate, String chat);



    Task findFirstByNameAndCreatedDateOrderByCreatedDateDesc(String p1, LocalDate previousWorkingDate);

    void deleteByCreatedDateGreaterThanEqualAndNameIn(LocalDate today, List<String> p1);

    List<Task> findByTeamId(Long teamId);

    List<Task> findByCreatedDateAndShift(LocalDate localDate, String shift);

    List<Task> findByCreatedDate(LocalDate localDate);

    void deleteByCreatedDateGreaterThanEqualAndNameInAndTeamId(LocalDate today, List<String> p1, Long teamId);

    List<Task> findByNameAndCreatedDateAndShiftAndTeamId(String chat, LocalDate localDate, String shift, Long teamId);

    List<Task> findByNameAndCreatedDateAndTeamId(String p1, LocalDate localDate, Long teamId);

    Task findFirstByNameAndCreatedDateAndTeamId(String p1, LocalDate previousWorkingDate, Long teamId);
}
