package com.example.engineer.repository;

import com.example.engineer.model.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngineerRepository extends JpaRepository<Engineer, Long> {
    Engineer findEngineerById(Long id);
    List<Engineer> findByTeamId(Long teamId);
}
