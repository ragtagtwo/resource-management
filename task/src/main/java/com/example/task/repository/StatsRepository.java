package com.example.task.repository;

import com.example.task.model.Stats;
import com.example.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {



    List<Stats> findByStartDateAndTeamID(LocalDate startOfQuarter, int teamId);
}
