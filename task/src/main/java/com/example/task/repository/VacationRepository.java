package com.example.task.repository;

import com.example.task.model.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRepository extends JpaRepository<Vacation, Long> {

    List<Vacation> findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long engineerId, LocalDate startDate, LocalDate endDate);
    List<Vacation> findByTeamId(Long teamId);
}